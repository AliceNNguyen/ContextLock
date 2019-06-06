//package com.amirarcane.lockscreen.fingerprint;
package com.lmu.alicenguyen.contextlock.fingerprint;


/**
 * Created by Arcane on 7/11/2017.
 */

public interface FingerPrintListener {

    void onSuccess();

    void onFailed();

    void onError(CharSequence errorString, int errMsgId);

    void onHelp(CharSequence helpString);

}

