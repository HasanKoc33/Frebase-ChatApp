package com.hasan.firebase_chatapp.Fragments;

import com.hasan.firebase_chatapp.Notifications.MyResponse;
import com.hasan.firebase_chatapp.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=\tAAAAcOYrj0c:APA91bH6FDxVl9lR_uHfGUNrrt6g0vkurSSQTPpaQQEB_TfdD4H0wPYsWuRMSL1vEs-7Klfr_sqcBORORzGf0XrgYaM9VLuzcdYwMpHq1BuL-SMN_WwgW43ujxwYCBxqJbhrbyYjLfbE"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);

}
