package chat.notification;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {


    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAhLs0WEE:APA91bF-I5DcE7Hbs97KKz6Gs77kVqjvgqURbF3N19IYFnBqa3KJM4wIdjcPLfQmC1tBKRlZoyVQnzs0c8-6nLPmm5zh9y1fcju-KXNbhLK3d3g7UF5ZKt4Yq8SJWFdF9-9hEU6wNtrK"
            }
    )


    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);


}
