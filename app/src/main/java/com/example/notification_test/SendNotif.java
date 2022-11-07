package com.example.notification_test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.notification_test.SendNotification.APIService;
import com.example.notification_test.SendNotification.Client;
import com.example.notification_test.SendNotification.Data;
import com.example.notification_test.SendNotification.MyResponse;
import com.example.notification_test.SendNotification.NotificationSender;
import com.example.notification_test.SendNotification.Token;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SendNotif extends AppCompatActivity {
    EditText UserTB,Title,Message;
    Button send, logoutB;
    private APIService apiService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        logoutB = findViewById(R.id.logout_b);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_notif);
        UserTB=findViewById(R.id.UserID);
        Title=findViewById(R.id.Title);
        Message=findViewById(R.id.Message);
        send=findViewById(R.id.button);

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
//Initialize the object of APIService with client class

//        logoutB.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                FirebaseAuth.getInstance().signOut();
//                startActivity(new Intent(SendNotif.this,MainActivity.class));
////                finish();
//            }
//        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseDatabase.getInstance().getReference().child("Tokens").child(UserTB.
                        getText().toString().trim()).child("token").addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String usertoken=dataSnapshot.getValue(String.class);
                                sendNotifications(usertoken,Title.getText().toString().trim(),Message.getText().toString().trim());

                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }
        });
        UpdateToken();
    }

    private void UpdateToken(){
        final String[] rToken = {""};
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                rToken[0] = task.getResult();
//                Toast.makeText(SendNotif.this, rToken[0] , Toast.LENGTH_SHORT).show();
                Token token= new Token(rToken[0]);
                FirebaseDatabase.getInstance().getReference("Tokens").child(FirebaseAuth.getInstance().getUid()).setValue(token);
            }
        });
    }
    //This Method Sends the notifications combining all class of
    //SendNotificationPack Package work together
    public void sendNotifications(String usertoken, String title, String message) {
        Data data = new Data(title, message);
        NotificationSender sender = new NotificationSender(data, usertoken);
        apiService.sendNotifcation(sender).enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(Call<MyResponse> call,
                    Response<MyResponse> response) {
                if (response.code() == 200) {
                    assert response.body() != null;
                    if (response.body().success != 1) {
                        Toast.makeText(SendNotif.this, "Failed", Toast.LENGTH_LONG);
                    }else {
                        Toast.makeText(SendNotif.this, "Success", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onFailure(Call<MyResponse> call, Throwable t) {
            }
        });
    }
}
