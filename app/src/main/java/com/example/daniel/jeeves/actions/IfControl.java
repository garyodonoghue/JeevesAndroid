package com.example.daniel.jeeves.actions;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.daniel.jeeves.ActionExecutorService;
import com.example.daniel.jeeves.ExpressionParser;
import com.example.daniel.jeeves.firebase.FirebaseExpression;
import com.example.daniel.jeeves.ApplicationContext;

import java.util.ArrayList;

/**
 * Created by Daniel on 08/06/15.
 */
public class IfControl extends FirebaseControl {
    public ArrayList<FirebaseAction> controlactions = new ArrayList<FirebaseAction>();
  //  ActionExecutorService mService;
    boolean mBound = false;

    @Override
    public void execute() {
        Log.d("CONTROLIF", "IF CONTROL");
      //  FirebaseExpression expression = getcondition();
        Context app = ApplicationContext.getContext();
        ArrayList<FirebaseAction> toExecute = new ArrayList<FirebaseAction>();
        ExpressionParser parser = new ExpressionParser(ApplicationContext.getContext());
        FirebaseExpression expression = null;
        expression = getcondition();

        controlactions = (ArrayList<FirebaseAction>) getactions();
        Intent actionIntent = new Intent(app,ActionExecutorService.class);
        actionIntent.putExtra("com/example/daniel/jeeves/actions",controlactions);
        actionIntent.putExtra("expression",expression);
        actionIntent.putExtra("controltype","if");

        app.startService(actionIntent);
    }
    @Override
    public ArrayList<FirebaseAction> getControlActions() {
        return controlactions;
   }
}