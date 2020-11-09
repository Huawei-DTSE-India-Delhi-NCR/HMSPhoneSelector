package com.example.hmsphoneselector;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import com.example.hmsphoneselector.databinding.UserdetailsBinding;
import com.huawei.agconnect.auth.AGConnectAuth;

public class UserDetails extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UserdetailsBinding userdetailsBinding = DataBindingUtil.setContentView(this, R.layout.userdetails);
        Bundle bundle=getIntent().getExtras();
        String response=bundle.getString("userData");
        userdetailsBinding.response.setText(response);
        userdetailsBinding.signout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.signout) {
            if(AGConnectAuth.getInstance().getCurrentUser()!=null){
                AGConnectAuth.getInstance().signOut();
                Toast.makeText(getApplicationContext(),getResources().getString(R.string.user_signOut),Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}
