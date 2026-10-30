#define _GNU_SOURCE
#include <android/log.h>
#include <dlfcn.h>
#include <jni.h>
#include <link.h>
#include <stdint.h>
#include <string.h>

#define TAG "SkiSafari-Unlock"
#define LOGI(message) __android_log_write(ANDROID_LOG_INFO, TAG, message)
#define LOGE(message) __android_log_write(ANDROID_LOG_ERROR, TAG, message)

// RVAs recovered from the exact libil2cpp.so SHA-256 enforced by the Morphe
// RawResourcePatch. These are virtual addresses (ELF image base zero), not
// file offsets. The original library remains byte-for-byte unchanged.
#define GAMESTATE_GET_PERSISTENT_PROFILE_RVA 0xBCDE98u
#define SHOP_ITEM_HELPER_GET_INSTANCE_RVA   0xC8DB68u
#define SHOP_ITEM_HELPER_UNLOCK_ALL_RVA     0xC8DC94u

typedef struct {
    uintptr_t base;
} Il2CppImage;

static int g_unlock_called;
static int g_profile_ready_logged;
static void *g_attached_il2cpp_thread;

static int find_il2cpp(struct dl_phdr_info *info, size_t size, void *data) {
    (void) size;
    if (info->dlpi_name != NULL && strstr(info->dlpi_name, "libil2cpp.so") != NULL) {
        ((Il2CppImage *) data)->base = (uintptr_t) info->dlpi_addr;
        return 1;
    }
    return 0;
}

JNIEXPORT jint JNICALL
Java_lab_skisafari_SkiSafariUnlock_tryUnlock(JNIEnv *env, jclass clazz) {
    (void) env;
    (void) clazz;

    if (__atomic_load_n(&g_unlock_called, __ATOMIC_ACQUIRE)) return 4;

    Il2CppImage image = {0};
    dl_iterate_phdr(find_il2cpp, &image);
    if (image.base == 0) return 0;

    if (g_attached_il2cpp_thread == NULL) {
        typedef void *(*DomainGet)(void);
        typedef void *(*ThreadAttach)(void *domain);
        DomainGet domain_get = (DomainGet) dlsym(RTLD_DEFAULT, "il2cpp_domain_get");
        ThreadAttach thread_attach = (ThreadAttach) dlsym(RTLD_DEFAULT, "il2cpp_thread_attach");
        if (domain_get == NULL || thread_attach == NULL) {
            LOGE("Errore: API IL2CPP domain/thread attach non esportate.");
            return -1;
        }
        void *domain = domain_get();
        if (domain == NULL) return 0;
        g_attached_il2cpp_thread = thread_attach(domain);
        if (g_attached_il2cpp_thread == NULL) {
            LOGE("Errore: thread Android non collegato al dominio IL2CPP.");
            return -1;
        }
    }

    typedef void *(*StaticGetter)(void *method_info);
    typedef void (*UnlockMethod)(void *instance, void *method_info);

    StaticGetter get_profile = (StaticGetter)
            (image.base + GAMESTATE_GET_PERSISTENT_PROFILE_RVA);
    StaticGetter get_shop_helper = (StaticGetter)
            (image.base + SHOP_ITEM_HELPER_GET_INSTANCE_RVA);
    UnlockMethod unlock_all = (UnlockMethod)
            (image.base + SHOP_ITEM_HELPER_UNLOCK_ALL_RVA);

    void *profile = get_profile(NULL);
    if (profile == NULL) return 1;

    if (!__atomic_exchange_n(&g_profile_ready_logged, 1, __ATOMIC_ACQ_REL)) {
        LOGI("Profilo persistente pronto.");
    }

    void *shop_helper = get_shop_helper(NULL);
    if (shop_helper == NULL) return 2;

    if (__atomic_exchange_n(&g_unlock_called, 1, __ATOMIC_ACQ_REL)) return 4;
    LOGI("Avvio ShopItemHelper.UnlockAllItems().");
    unlock_all(shop_helper, NULL);
    LOGI("UnlockAllItems ritornato; il metodo chiama GameState.Save quando cambia gli sblocchi.");
    return 3;
}
