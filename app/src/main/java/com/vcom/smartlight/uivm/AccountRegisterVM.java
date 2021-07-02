package com.vcom.smartlight.uivm;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.QuerySendDetailsRequest;
import com.aliyun.dysmsapi20170525.models.QuerySendDetailsResponse;
import com.aliyun.dysmsapi20170525.models.QuerySendDetailsResponseBody;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teautil.Common;
import com.vcom.smartlight.model.MessageEvent;
import com.vcom.smartlight.request.AliSms;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Banlap on 2021/6/17
 */
public class AccountRegisterVM extends AndroidViewModel {

    private AccountRegisterCallBack callBack;

    public AccountRegisterVM(@NonNull Application application) {
        super(application);
    }

    public void setCallBack(AccountRegisterCallBack callBack) {
        this.callBack = callBack;
    }

    public void viewBack() {
        callBack.viewBack();
    }

    public void sendSMSCode() {
        callBack.sendSMSCode();
    }

    public void sendSMSCodeRequest(String phoneNumber) {
        int randomCode = (int) Math.random();
        /*new Thread(() -> {
            try {
                //banlap: 阿里云短信服务 akId和 akSt
                String accessKeyId = "LTAI7HviQHbtnuOM";
                String accessKeySecret = "OOWSKmxaUSu9TvyfREYEbstJcBEiVe";
                Client client = AliSms.createClient(accessKeyId, accessKeySecret);
                //banlap: 阿里云短信服务 模板格式（必须在平台上申请）
                String signName = "唯康智控"; //模板名称
                String templateCode = "SMS_218276430";  //模板ID  模版内容: 验证码${code}，您正在进行身份验证，打死不要告诉别人哦！
                String templateParam = "{\"code\":\"000000\"}"; //${code}值显示

                SendSmsRequest sendSmsRequest = new SendSmsRequest()
                        .setPhoneNumbers(phoneNumber)
                        .setSignName(signName)
                        .setTemplateCode(templateCode)
                        .setTemplateParam(templateParam);

                //client.sendSms(sendSmsRequest);

               *//* SendSmsResponse sendSmsResponse = client.sendSms(sendSmsRequest);

                if(!Common.equalString(sendSmsResponse.body.code, "OK")) {
                    Log.e("Ali_sms:", "错误：" + sendSmsResponse.body.message);
                    return;
                }
                String bizId = sendSmsResponse.body.bizId;
                Common.sleep(10000);

                QuerySendDetailsRequest request = new QuerySendDetailsRequest()
                        .setPhoneNumber(phoneNumber)
                        .setBizId(bizId)
                        .setSendDate("2021-06-18")
                        .setPageSize(10L)
                        .setCurrentPage(1L);*//*

                // QuerySendDetailsResponse response = client.querySendDetails(request);
                //Log.e("Ali_sms", "" + response.body.message + response.body.smsSendDetailDTOs);

                *//*QuerySendDetailsResponse response = client.querySendDetails(request);
                List<QuerySendDetailsResponseBody.QuerySendDetailsResponseBodySmsSendDetailDTOsSmsSendDetailDTO>
                        dtos = response.body.smsSendDetailDTOs.smsSendDetailDTO;

                for(QuerySendDetailsResponseBody.QuerySendDetailsResponseBodySmsSendDetailDTOsSmsSendDetailDTO dto : dtos) {
                    if(Common.equalString("" + dto.sendStatus + "", "3")) {
                        Log.e("Ali_sms", "" + dto.phoneNum + " 发送成功，接收时间: " + dto.receiveDate + "::" + dto.content);
                    } else if(Common.equalString("" + dto.sendStatus + "", "2")) {
                        Log.e("Ali_sms", "" + dto.phoneNum + " 发送失败" + "");
                    } else {
                        Log.e("Ali_sms", "" + dto.phoneNum + " 正在发送中..." + "");
                    }
                }*//*

            } catch (Exception e) {
                //e.printStackTrace();
                EventBus.getDefault().post(new MessageEvent(MessageEvent.sendMessageError));
            }
        }).start();*/
    }


    public interface AccountRegisterCallBack {
        void viewBack();
        void sendSMSCode();

    }
}
