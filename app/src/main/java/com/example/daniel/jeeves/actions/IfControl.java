package com.example.daniel.jeeves.actions;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.daniel.jeeves.ActionExecutorService;
import com.example.daniel.jeeves.ApplicationContext;
import com.example.daniel.jeeves.ExpressionParser;
import com.example.daniel.jeeves.firebase.FirebaseExpression;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Daniel on 08/06/15.
 */
public class IfControl extends FirebaseAction {
    public ArrayList<FirebaseAction> controlactions = new ArrayList<FirebaseAction>();
  //  ActionExecutorService mService;
    boolean mBound = false;

    public IfControl(Map<String,Object> params){
        setparams(params);
    }
    @Override
    public void execute() {
        Log.d("CONTROLIF", "IF CONTROL");
      //  FirebaseExpression expression = getcondition();
        Context app = ApplicationContext.getContext();
        ExpressionParser parser = new ExpressionParser(ApplicationContext.getContext());
        FirebaseExpression expression = null;
        expression = getcondition();
        Log.d("Description", getdescription());

        Log.d("Expression","Expression is " + expression);
        controlactions = (ArrayList<FirebaseAction>) getactions();
//        //Converting the actions into their correct types
//        ArrayList<FirebaseAction> actionsToPerform = new ArrayList<>();
//        for(FirebaseAction action : controlactions){
//            actionsToPerform.add(ActionUtils.create(action)); //Oh good lord really!?
//        }
        Intent actionIntent = new Intent(app,ActionExecutorService.class);
        if(this.getmanual())
            actionIntent.putExtra("manual",true);
        actionIntent.putExtra("com/example/daniel/jeeves/actions",controlactions);
        actionIntent.putExtra("expression",expression);
        actionIntent.putExtra("controltype","if");

        app.startService(actionIntent);
    }
 //   @Override
    public ArrayList<FirebaseAction> getControlActions() {
        return controlactions;
   }
}
