package progr3.mail.server.app;

import com.fasterxml.jackson.databind.ObjectMapper;

import progr3.mail.server.model.Response;

public class ResponseConstructor {
    private static ObjectMapper mapper = new ObjectMapper();

    public static Response success(String message, Object body) {
        var response = new Response();
        response.setStatus(Response.Status.OK);
        response.setMessage(message);
        response.setResult(Response.Result.SUCCESS);
        try {
            response.setBody(mapper.writeValueAsString(body));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public static Response internalServerError(String message, String body) {
        var response = new Response();
        response.setStatus(Response.Status.INTERNAL_SERVER_ERROR);
        response.setMessage(message);
        response.setResult(Response.Result.FAILURE);
        response.setBody(body);
        return response;
    }

    public static Response unauthorized(String message, String body) {
        var response = new Response();
        response.setStatus(Response.Status.UNAUTHORIZED);
        response.setMessage(message);
        response.setResult(Response.Result.FAILURE);
        response.setBody(body);
        return response;
    }

    public static Response badRequest(String message, String body) {
        var response = new Response();
        response.setStatus(Response.Status.BAD_REQUEST);
        response.setMessage(message);
        response.setResult(Response.Result.FAILURE);
        response.setBody(body);
        return response;
    }

    public static Response notFound(String message, String body) {
        var response = new Response();
        response.setStatus(Response.Status.NOT_FOUND);
        response.setMessage(message);
        response.setResult(Response.Result.FAILURE);
        response.setBody(body);
        return response;
    }

}
