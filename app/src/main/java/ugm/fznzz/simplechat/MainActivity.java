package ugm.fznzz.simplechat;

import androidx.appcompat.app.AppCompatActivity;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.ListenableFuture;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.simplechat.siChat;
import io.grpc.simplechat.ChatRoomGrpc;
import io.grpc.stub.StreamObserver;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    TextView tvPesan;
    EditText et_pesan,et_ip,et_port,et_nama;
    String temp,temp2,temp_ip;
    Integer temp_port;
    Snackbar snackbar;
    private Throwable failed;
    StreamObserver responseObserver;
    StreamObserver requestObserver;

    ManagedChannel channel;
    ChatRoomGrpc.ChatRoomStub asyncStub;
    ChatRoomGrpc.ChatRoomFutureStub futureStub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvPesan = findViewById(R.id.tv_pesan);
        et_pesan = findViewById(R.id.et_pesan);
        et_ip = findViewById(R.id.et_ip);
        et_port = findViewById(R.id.et_port);
        et_nama = findViewById(R.id.et_username);


    }

    public void onClick_send(View v)
    {
        if(TextUtils.isEmpty(et_pesan.getText().toString()))
        {
            et_pesan.setError("Silakan masukkan pesan anda");
        }
        else {
            temp = et_pesan.getText().toString();
            temp2 = et_nama.getText().toString();
            tvPesan.append(temp2+" : "+temp+"\n");
            et_pesan.setText("");

            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setUser(temp2);
            chatMessage.setText(temp);
            try {
                requestObserver.onNext(chatMessage);
            }
            catch (Exception e)
            {Log.d("error",e.getMessage());}
        }
    }
    private void initGrpcComm() {
        temp_ip = et_ip.getText().toString();
        temp_port = Integer.parseInt(et_port.getText().toString());
        //Channel builder
        channel = ManagedChannelBuilder
                .forAddress(temp_ip, temp_port)
                .usePlaintext()
                .build();
        futureStub = ChatRoomGrpc.newFutureStub(channel);
        asyncStub = ChatRoomGrpc.newStub(channel);
        grpcStreamMessage();
    }
    public void onClick_con(View v)
    {
        initGrpcComm();
        //Notifikasi snackbar
        snackbar = Snackbar.make(findViewById(R.id.coordinator),"Tersambung dengan "+ temp_ip + " port " +temp_port,Snackbar.LENGTH_LONG);
        snackbar.show();
    }
    public void onClick_dis(View v)
    {
        snackbar = Snackbar.make(findViewById(R.id.coordinator),"Terputus dengan "+temp_ip + " port " +temp_port,Snackbar.LENGTH_LONG);
        snackbar.show();
    }
    private void grpcStreamMessage() {
        // response observer
        responseObserver = new StreamObserver<siChat.Message>() {
            @Override
            public void onNext(siChat.Message value) {
                tvPesan.append(value.getUser()+" : "+value.getText());
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                tvPesan.append("Disconnected");
            }
        };

        // server server stream with response observer
        requestObserver = asyncStub.join(responseObserver);
    }
}
