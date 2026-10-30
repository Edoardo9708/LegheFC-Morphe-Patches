package lab.skisafari;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

/** Runtime bridge for the verified Ski Safari 2.5 ARM64 native helper. */
public final class SkiSafariUnlock {
    private static final String TAG = "SkiSafari-Unlock";
    private static final String PACKAGE = "com.yodo1.skisafarigp";
    private static final String PREFERENCES = "ski_safari_unlock_patch";
    private static final String APPLIED_KEY = "unlock_all_items_v1_applied";
    private static final long RETRY_MS = 500L;
    private static final long TIMEOUT_MS = 120_000L;

    private static boolean scheduled;

    private SkiSafariUnlock() { }

    public static synchronized void start(Activity activity) {
        if (activity == null) return;
        Context app = activity.getApplicationContext();
        if (!PACKAGE.equals(app.getPackageName())) {
            Log.e(TAG, "Errore: package non corrispondente.");
            return;
        }

        final PackageInfo packageInfo;
        try {
            packageInfo = app.getPackageManager().getPackageInfo(PACKAGE, 0);
        } catch (Exception e) {
            Log.e(TAG, "Errore: impossibile leggere la versione installata.");
            return;
        }
        long versionCode = android.os.Build.VERSION.SDK_INT >= 28
                ? packageInfo.getLongVersionCode() : packageInfo.versionCode;
        if (!"2.5".equals(packageInfo.versionName) || versionCode != 108L) {
            Log.e(TAG, "Errore: Ski Safari 2.5 (108) richiesto; procedura non avviata.");
            return;
        }

        Log.i(TAG, "Patch caricata per Ski Safari 2.5 (108).");
        if (app.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).getBoolean(APPLIED_KEY, false)) {
            Log.i(TAG, "Sblocco gia' completato e salvato; nessuna nuova chiamata.");
            return;
        }
        if (scheduled) return;

        try {
            System.loadLibrary("skisafariunlock");
        } catch (Throwable t) {
            Log.e(TAG, "Errore: libreria nativa Ski Safari Unlock non caricata.");
            return;
        }

        scheduled = true;
        final long startedAt = SystemClock.elapsedRealtime();
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override public void run() {
                final int result;
                try {
                    result = tryUnlock();
                } catch (Throwable t) {
                    Log.e(TAG, "Errore durante la chiamata nativa ShopItemHelper.");
                    scheduled = false;
                    return;
                }

                if (result == 3 || result == 4) {
                    boolean saved = app.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
                            .edit().putBoolean(APPLIED_KEY, true).commit();
                    if (saved) {
                        Log.i(TAG, "UnlockAllItems e' ritornato; marcatore one-shot persistito. Il salvataggio del gioco va verificato al riavvio.");
                    } else {
                        Log.e(TAG, "Funzione completata, ma marcatore one-shot non persistito; potra' essere ripetuta al prossimo avvio.");
                    }
                    scheduled = false;
                    return;
                }

                if (result < 0) {
                    Log.e(TAG, "Errore: target IL2CPP non pronto o non corrispondente.");
                    scheduled = false;
                    return;
                }

                if (SystemClock.elapsedRealtime() - startedAt >= TIMEOUT_MS) {
                    Log.e(TAG, "Errore: profilo o ShopItemHelper non pronto entro il timeout; sblocco non dichiarato.");
                    scheduled = false;
                    return;
                }
                handler.postDelayed(this, RETRY_MS);
            }
        });
    }

    /** 0=IL2CPP absent, 1=profile not loaded, 2=shop helper absent, 3=called, 4=already called. */
    private static native int tryUnlock();
}
