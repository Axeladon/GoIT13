import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

import task1.*;
import task2.Comment;
import task2.Post;
import Task3.UserToDo;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Main {
    public static void main(String[] args) {
        final String SERVER_URL = "https://jsonplaceholder.typicode.com/users";
        final int ID = 11;

        //task1
//        createNewUser(SERVER_URL);
//        editUserById(SERVER_URL, ID);
//        deleteUserById(SERVER_URL, ID);
//        getAllUsersInfo(SERVER_URL);
//        getUserById(SERVER_URL, ID);
//        getUserByUserName(SERVER_URL, "Kamrfen");

        //task2
//        displayLastPostCommentsToFile(ID);

        //task3
//        displayLastPostCommentsToFile(ID);
    }

    public static void displayLastPostCommentsToFile(int user_id) {
        Gson gson = new Gson();

        final String postsURL = "https://jsonplaceholder.typicode.com/users/" + user_id + "/posts";
        HttpResponse<String> postResponse
                = sendHttpCommandRequest(postsURL, "GET", HttpRequest.BodyPublishers.noBody());
        JsonArray jsonPostArray = gson.fromJson(postResponse.body(), JsonArray.class);

        if (jsonPostArray.size() == 0) {
            System.out.println("Posts don't exist!");
            return;
        }

        Long maxPostID = StreamSupport.stream(jsonPostArray.spliterator(), false)
                .map(jsonElement -> gson.fromJson(jsonElement, Post.class))
                .map(Post::getId)
                .reduce(0L, Long::max);

        final String commentsURL = "https://jsonplaceholder.typicode.com/posts/" + maxPostID + "/comments";
        HttpResponse<String> commentsResponds
                = sendHttpCommandRequest(commentsURL, "GET", HttpRequest.BodyPublishers.noBody());
        JsonArray jsonCommentArray = gson.fromJson(commentsResponds.body(), JsonArray.class);

        List<Comment> comments = StreamSupport.stream(jsonCommentArray.spliterator(), false)
                .map(jsonElement -> gson.fromJson(jsonElement, Comment.class))
                .collect(Collectors.toList());

        for (Comment comment : comments) {
            System.out.println("postId: " + comment.getPostId());
            System.out.println("id: " + comment.getId());
            System.out.println("name: " + comment.getName());
            System.out.println("email: " + comment.getEmail());
            System.out.println("body: " + comment.getBody());
            System.out.println();
        }

        String filename = "src//main//java//task2//user-" + user_id + "-post-" + maxPostID + "-comments.json";
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
            writer.write(commentsResponds.body());
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void displayOpenTasksForUserById(int user_id) {
        final String URL = "https://jsonplaceholder.typicode.com/users/" + user_id + "/todos";
        Gson gson = new Gson();

        HttpResponse<String> response = sendHttpCommandRequest(URL, "GET", HttpRequest.BodyPublishers.noBody());
        JsonArray jsonArray = gson.fromJson(response.body(), JsonArray.class);

        if (jsonArray.size() == 0) {
            System.out.println("Users don't exist!");
            return;
        }

        List<UserToDo> users = StreamSupport.stream(jsonArray.spliterator(), false)
                .map(jsonElement -> gson.fromJson(jsonElement, UserToDo.class))
                .filter(user -> !user.isCompleted())
                .collect(Collectors.toList());

        for (UserToDo user : users) {
            System.out.println("userId: " + user.getUserId());
            System.out.println("id: " + user.getId());
            System.out.println("title: " + user.getTitle());
            System.out.println("completed: " + user.isCompleted());
            System.out.println();
        }
    }
    private static HttpResponse<String> sendHttpCommandRequest(String url, String method, HttpRequest.BodyPublisher bp) {
        try {
            HttpRequest request = HttpRequest.newBuilder(new URI(url))
                    .method(method, bp)
                    .build();
            HttpClient client = HttpClient.newHttpClient();
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    public static void getUserByUserName(String serverURL, String userName) {
        String url = serverURL + "?username=" + userName;
        HttpResponse<String> response = sendHttpCommandRequest(url, "GET", HttpRequest.BodyPublishers.noBody());

        if (response.statusCode() == 200) {
            if (response.body().equals("[]")) {
                System.out.println("The user does not exist!");
                return;
            }
            Gson gson = new Gson();
            JsonArray jsonArray = gson.fromJson(response.body(), JsonArray.class);
            for (int i = 0; i < jsonArray.size(); i++) {
                showAllUserInfo(gson.fromJson(jsonArray.get(i), User.class));
            }
        } else {
            System.out.println("Error code: " + response.statusCode());
        }
    }
    public static void getUserById(String serverURL, int id) {
        String url = serverURL + "/" + id;
        HttpResponse<String> response = sendHttpCommandRequest(url, "GET", HttpRequest.BodyPublishers.noBody());

        int code = response.statusCode();
        if (code == 200) {
            showAllUserInfo(new Gson().fromJson(response.body(), User.class));
        } else if (code == 404) {
            System.out.println("The user does not exist!");
        } else {
            System.out.println("Error code: " + response.statusCode());
        }
    }
    public static void getAllUsersInfo(String serverURL) {
        HttpResponse<String> response
                = sendHttpCommandRequest(serverURL, "GET", HttpRequest.BodyPublishers.noBody());

        if (response.statusCode() == 200) {
            List<User> users = new ArrayList<>();
            Gson gson = new Gson();

            JsonArray jsonArray = gson.fromJson(response.body(), JsonArray.class);
            for (int i = 0; i < jsonArray.size(); i++) {
                users.add(gson.fromJson(jsonArray.get(i), User.class));
                showAllUserInfo(users.get(i));
                System.out.println();
            }
        } else {
            System.out.println("Error code: " + response.statusCode());
        }
    }
    private static void showAllUserInfo(User user) {
        if (user == null)
            throw new NullPointerException("User not found!");

        System.out.println("id: " + user.getId());
        System.out.println("name: " + user.getName());
        System.out.println("username: " + user.getUsername());
        System.out.println("email: " + user.getEmail());
        System.out.println("address: ");
        System.out.println("street: " + user.getAddress().getStreet());
        System.out.println("suite: " + user.getAddress().getSuite());
        System.out.println("city: " + user.getAddress().getCity());
        System.out.println("zipcode: " + user.getAddress().getZipcode());
        System.out.println("geo: ");
        System.out.println("lat: " + user.getAddress().getGeo().getLat());
        System.out.println("lng: " + user.getAddress().getGeo().getLng());
        System.out.println("phone: " + user.getPhone());
        System.out.println("website: " + user.getWebsite());
        System.out.println("company: ");
        System.out.println("name: " + user.getCompany().getName());
        System.out.println("catchPhrase: " + user.getCompany().getCatchPhrase());
        System.out.println("bs: " + user.getCompany().getBs());
    }
    public static void editUserById(String serverURL, int id) {

        Gson gson = new Gson();
        User newUser = User.builder()
                .name("Alex")
                .username("Axeladon")
                .email("newbie@gmail.com")
                .phone("937-99-92")
                .build();

        String url = serverURL + "/" + id;
        HttpResponse<String> response
                = sendHttpCommandRequest(url, "PUT", HttpRequest.BodyPublishers.ofString(gson.toJson(newUser)));

        System.out.println("Status code: " + response.statusCode());
        System.out.println(response.body());
    }
    public static void createNewUser(String serverUrl) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        User newUser = User.builder()
                .name("Alex")
                .username("Axeladon")
                .email("alex@gmail.com")
                .address(Address.builder()
                        .street("Glory for Ukraine")
                        .suite("2")
                        .city("Kyiv")
                        .zipcode("20-444")
                        .geo(Geo.builder()
                                .lat(-37.3159)
                                .lng(81.1496)
                                .build())
                        .build())
                .phone("232-236362-23424234")
                .website("www.go-home.com")
                .company(Company.builder()
                        .name("Black & White")
                        .catchPhrase("Make your dreams come true!")
                        .bs("whatever you want")
                        .build())
                .build();

        HttpResponse<String> response
                = sendHttpCommandRequest(serverUrl, "POST", HttpRequest.BodyPublishers.ofString(gson.toJson(newUser)));

        if (response.statusCode() == 201) {
            System.out.println(response.body());
        } else {
            System.out.println("Error code: " + response.statusCode());
        }
    }
    public static void deleteUserById(String serverURL, int id) {
        String url = serverURL + "/" + id;
        HttpResponse<String> response = sendHttpCommandRequest(url, "DELETE", HttpRequest.BodyPublishers.noBody());
        System.out.println("Status Code: " + response.statusCode());
    }
}