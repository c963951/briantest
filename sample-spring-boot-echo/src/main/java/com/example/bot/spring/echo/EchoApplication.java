package com.example.bot.spring.echo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.sound.sampled.UnsupportedAudioFileException;

import com.linecorp.bot.model.message.*;
import com.linecorp.bot.model.profile.MembersIdsResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.bot.spring.echo.service.ReplyService;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.PostbackEvent;
import com.linecorp.bot.model.event.message.LocationMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.RoomSource;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

import lombok.NonNull;

@SpringBootApplication
@LineMessageHandler
public class EchoApplication {

    private final String channelToken = "OD6ub5Qyystuid9ouEmNPBRLFmQTyeAbEX9ngG3WB9Scma4cDIM5qanrZ5dmJgnoKPxGwMQlsyDC8jm3p7LMLinTKRJDuMBrJ4ACM0egQIppZBoCGtCYA0rgBp8PSb8EkJppGlP0BhaWFVaeiyQddwdB04t89/1O/w1cDnyilFU=";

    @Autowired
    private LineMessagingClient lineMessagingClient;

    @Autowired
    ReplyService rplys;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(EchoApplication.class, args);
    }

    @EventMapping
    public void handleLocationMessageEvent(MessageEvent<LocationMessageContent> event) throws Exception {
        List<Message> Messages = new ArrayList<Message>();
        Source source = event.getSource();
        String pushId = source.getUserId();
        if (source instanceof GroupSource) {
            pushId = ((GroupSource)source).getGroupId();
        }
        else if (source instanceof RoomSource) {
            pushId = ((RoomSource)source).getRoomId();
        }
        Messages.add(getPost(event.getMessage().getLatitude(), event.getMessage().getLongitude()));
        push(channelToken, pushId, Messages);
    }

    @EventMapping
    public void handleDefaultMessageEvent(MessageEvent<TextMessageContent> event) throws Exception {
        Source source = event.getSource();
        List<Message> Messages = new ArrayList<Message>();
        String pushId = source.getUserId();
        if (source instanceof GroupSource) {
            pushId = ((GroupSource)source).getGroupId();
        }
        else if (source instanceof RoomSource) {
            pushId = ((RoomSource)source).getRoomId();
        }

        String message = event.getMessage().getText();
        String keyworad = message.substring(1);
        if (message.equals("@所有人") && source instanceof GroupSource) {
            CompletableFuture<MembersIdsResponse> member = lineMessagingClient.getGroupMembersIds(pushId,null);
            Messages.add(tagAll(member));
        }
        else if (message.startsWith("%")) {
            Messages.add(PTT(keyworad));
        }
        else if (message.startsWith("##")) {
            Messages.add(TTs(message.substring(2)));
        }
        else if (message.startsWith("#")) {
            Messages.add(Horoscope(keyworad));
        }
        else if (message.startsWith("&")) {
            Messages.addAll(getYoutube(keyworad));
        }
        else if (message.startsWith("$")) {
            Messages.add(Weather(keyworad));
        }
        else if (message.startsWith("-r ")) {
            Messages.add(getRate(StringUtils.removeStart(message, "-r ")));
        }
        else if (message.startsWith("-gas")) {
            Messages.add(getGas());
        }
        else if (message.startsWith("news")) {
            Messages.add(getNews());
        }
        else if (message.startsWith("google")) {
            Messages.add(getSearch(StringUtils.removeStart(message, "google ")));
        }
        else if (message.startsWith("spotify")) {
            Messages.addAll(getSpotify(StringUtils.removeStart(message, "spotify ")));
        }
        else if (message.startsWith("speech")) {
            Messages.add(getSpeech(StringUtils.removeStart(message, "speech ")));
        }
        // else if (message.startsWith("-t")) {
        // Messages.add(getTranslateCard(StringUtils.removeStart(message, "-t
        // ")));
        // }
        else if (message.equals("Botbye")) {
            if (source instanceof GroupSource) {
                lineMessagingClient.leaveGroup(((GroupSource)source).getGroupId()).get();
            }
            else if (source instanceof RoomSource) {
                lineMessagingClient.leaveRoom(((RoomSource)source).getRoomId()).get();
            }
            return;
        }
        if (Messages.isEmpty()) {
            return;
        }
        push(channelToken, pushId, Messages);
    }

    @EventMapping
    public void handlePostbackEvent(PostbackEvent event) throws Exception {
        Source source = event.getSource();
        String pushId = source.getUserId();
        String data = event.getPostbackContent().getData();
        List<Message> Messages = new ArrayList<>();
        if (source instanceof GroupSource) {
            pushId = ((GroupSource)source).getGroupId();
        }
        else if (source instanceof RoomSource) {
            pushId = ((RoomSource)source).getRoomId();
        }
        System.out.println("data=================="+data);
        Messages.add(getPlaces(data));


        push(channelToken, pushId, Messages);
    }

    public TextMessage tagAll(CompletableFuture<MembersIdsResponse> member) throws ExecutionException, InterruptedException {
        return new TextMessage(rplys.getTagMessage(member));
    }

    public TextMessage PTT(String message) {
        return new TextMessage(rplys.getPttMessage(message));
    }

    public TextMessage Weather(String message) {
        return new TextMessage(rplys.getWeather(message));
    }

    public AudioMessage TTs(String message) throws IOException {
        return rplys.getTTs(message);
    }

    public AudioMessage getSpeech(String message) throws IOException, UnsupportedAudioFileException {
        return rplys.getCloudTTs(message, null);
    }

    public TextMessage Horoscope(String message) {
        return new TextMessage(rplys.getHoroscope(message));
    }

    public List<Message> getYoutube(String message) throws Exception {
        return rplys.getYoutube(message);
    }

    public TemplateMessage getRestaurant(double lat, double lng) throws Exception {
        return rplys.getRestaurant(lat, lng);
    }

    public TextMessage getRate(String message) {
        return new TextMessage(rplys.getRateMessage(message));
    }

    public TextMessage getGas() {
        return new TextMessage(rplys.getGasMessage());
    }

    public TextMessage getNews() throws IOException {
        return new TextMessage(rplys.getNewsMessage());
    }

    public TextMessage getSearch(String message) throws IOException {
        return new TextMessage(rplys.getSearchMessage(message));
    }

    public List<Message> getSpotify(String message) throws Exception {
        return rplys.getSpotify(message);
    }

    public FlexMessage getPost(double lat, double lng) throws Exception {
        return rplys.getCarousel(lat, lng);
    }

    public TemplateMessage getPlaces(String data) throws Exception {
        return rplys.getGooglePlaces(data);
    }

    public TemplateMessage getTranslateCard(String data) throws Exception {
        return rplys.getTranslateCard(data);
    }

    public List<Message> getTranslate(String message) throws Exception {
        return rplys.getTranslate(message);
    }

    private void push(@NonNull String channelToken, @NonNull String pushId, @NonNull List<Message> messages)
            throws Exception {
        try {
            CompletableFuture<BotApiResponse> response = lineMessagingClient
                    .pushMessage(new PushMessage(pushId, messages));
            // Response<BotApiResponse> response =
            // LineMessagingServiceBuilder
            // .create(channelToken)
            // .build()
            // .pushMessage((new PushMessage(pushId, messages)))
            // .execute();
            // System.out.println(response.code() + " " + response.message());
            System.out.println(response.get().getMessage() + " " + response.get().getDetails().toString());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
