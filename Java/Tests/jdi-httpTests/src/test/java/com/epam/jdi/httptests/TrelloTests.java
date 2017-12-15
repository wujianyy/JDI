package com.epam.jdi.httptests;

import com.epam.commons.map.MapArray;
import com.epam.http.requests.*;
import io.restassured.response.ValidatableResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.epam.http.requests.RequestData.requestBody;
import static com.epam.http.requests.RequestData.requestData;
import static com.epam.http.requests.RequestData.requestParams;
import static com.epam.http.requests.ServiceInit.init;
import static com.epam.jdi.httptests.TrelloApi.getBoardCardById;
import static com.epam.jdi.httptests.TrelloApi.getBoardCardsList;
import static java.lang.String.format;
import static org.apache.commons.lang3.RandomStringUtils.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsEqual.equalTo;

public class TrelloTests {

    public static final String BOARD_ID = "5a27e3b62fef5d3a74dca48a";
    public static final String CARD_UNIQUE_ID = "5a27e722e2f04f3ab6924931";

    @BeforeClass
    public void initService() {
        init(TrelloApi.class);
    }

    @Test
    public void createNewBoardTest() {
        String boardName = "Lorem ipsum board " + random(12, true, true);
        RestResponse response = TrelloApi.boardsPost
                .call(requestBody(format("{\"name\": \"%s\"}", boardName)));
        response.isOk().body("name", equalTo(boardName));
    }

    @Test
    public void getBoardById() {
        RestResponse response = TrelloApi
                .getBoardById
                .call(requestParams(BOARD_ID));
        response.isOk().body("id", equalTo(BOARD_ID));
    }

    @Test
    public void getBoardCardsList() {
        getBoardCardsList.call(requestParams(BOARD_ID))
            .isOk().body("name.size()", equalTo(6));
    }

    @Test
    public void getCardByShortId() {
        getBoardCardById.call(requestParams(BOARD_ID, "1"))
            .isOk().assertThat().body("name", equalTo("Lorem ipsum dolor sit amet"));
    }

    @Test
    public void postNewCommentToCard() {
        String newComment = "New comment" + random(7, true, false);
        RestResponse response = TrelloApi
                .postNewCommentToCard
                .call(requestData(d -> {
                    d.pathParams.add(CARD_UNIQUE_ID);
                    d.body = format("{\"text\": \"%s\"}", newComment);}
                ));
        response.isOk()
                .body("data.text", containsString(newComment));

    }

    @Test
    public void getAllUserBoards() {

        RestResponse restResponse = TrelloApi
                .getAllMemberBoards
                .call(requestParams("jdiframwork"));
        restResponse.assertThat()
                .body("name.size()", greaterThan(4));
    }

    @Test
    public void getCardByUniqueId() {
        RequestData data = requestData(d -> {
            d.queryParams.add("fields", "url,shortUrl");
            d.pathParams.add(CARD_UNIQUE_ID);});

        RestResponse restResponse = TrelloApi
                .getCardByUniqueId
                .call(data);

        restResponse.assertBody(new Object[][]{
                {"url", containsString("https://trello.com/c/SSFPAlkB/1-lorem-ipsum-dolor-sit-amet")},
                {"shortUrl", containsString("https://trello.com/c/SSFPAlkB")},
                {"id", equalTo(CARD_UNIQUE_ID)},
                {"keySet().size()", is(3)}
        });
    }
}