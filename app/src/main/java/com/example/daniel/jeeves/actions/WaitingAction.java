package com.example.daniel.jeeves.actions;

import android.content.Context;
import android.util.Log;

import com.example.daniel.jeeves.ApplicationContext;

/**
 * Created by Daniel on 27/05/15.
 */
public class WaitingAction extends FirebaseAction {

    @Override
    public void execute() {
        Log.d("ACTIONWAIT", "SOMEHOW GONNA WAIT BEFORE EXECUTING");
        Context app = ApplicationContext.getContext();

    }
}