package com.eusleats.eusleats.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Server-side access control for every /api/** request.
 *
 * - Public routes (login, register, browsing stores and menus) need no token.
 * - Everything else needs "Authorization: Bearer <token>".
 * - A student can only touch their own orders, and a store can only touch
 *   its own menu and orders. Admin routes need an ADMIN token.
 *
 * The verified identity is stored in request attributes so controllers can
 * read it with @RequestAttribute (the client can never choose it).
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String ATTR_ROLE = "auth.role";
    public static final String ATTR_REF = "auth.refId";

    private final TokenService tokenService;

    public AuthInterceptor(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        String method = request.getMethod();

        // Browsers send OPTIONS first (CORS pre-flight). It carries no token.
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        String[] seg = segments(request.getRequestURI());

        if (seg.length < 2 || !"api".equals(seg[0])) {
            return true;
        }

        if (isPublic(method, seg)) {
            return true;
        }

        TokenService.Claims claims = null;
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            claims = tokenService.verify(header.substring(7).trim());
        }

        if (claims == null) {
            reject(response, 401, "Please log in again.");
            return false;
        }

        if (!isAllowed(method, seg, claims)) {
            reject(response, 403, "You are not allowed to do this.");
            return false;
        }

        request.setAttribute(ATTR_ROLE, claims.role());
        request.setAttribute(ATTR_REF, claims.refId());

        return true;
    }

    // -------------------------------------------------------------
    // Which routes are public?
    // -------------------------------------------------------------
    private boolean isPublic(String method, String[] seg) {

        boolean get = "GET".equalsIgnoreCase(method);
        boolean post = "POST".equalsIgnoreCase(method);
        String area = seg[1];

        if (get && seg.length == 2 && area.equals("test")) {
            return true;
        }

        if (get && seg.length == 2 && area.equals("food-stores")) {
            return true;
        }

        if (get && seg.length == 4
                && area.equals("food-items") && seg[2].equals("store")) {
            return true;
        }

        if (post && seg.length == 3) {
            boolean accountArea = area.equals("students")
                    || area.equals("stores")
                    || area.equals("admin");

            if (accountArea && (seg[2].equals("login")
                    || (!area.equals("admin") && seg[2].equals("register")))) {
                return true;
            }
        }

        return false;
    }

    // -------------------------------------------------------------
    // Who may call each protected route?
    // -------------------------------------------------------------
    private boolean isAllowed(
            String method, String[] seg, TokenService.Claims claims) {

        String role = claims.role();
        Integer ref = claims.refId();
        String area = seg[1];

        boolean get = "GET".equalsIgnoreCase(method);
        boolean post = "POST".equalsIgnoreCase(method);
        boolean put = "PUT".equalsIgnoreCase(method);
        boolean delete = "DELETE".equalsIgnoreCase(method);

        switch (area) {



            case "students":
                // GET /api/students/me
                return get && seg.length == 3 && seg[2].equals("me")
                        && role.equals("STUDENT");

            case "stores":
                // GET /api/stores/me
                return get && seg.length == 3 && seg[2].equals("me")
                        && role.equals("STORE");

            case "food-items":
                if (!role.equals("STORE")) {
                    return false;
                }

                // POST /api/food-items (the store id is taken from the token)
                if (post && seg.length == 2) {
                    return true;
                }

                // PUT/DELETE /api/food-items/{foodId}/{storeId}
                // PUT /api/food-items/availability/{foodId}/{storeId}
                if ((put || delete) && seg.length >= 4) {
                    return sameId(seg[seg.length - 1], ref);
                }

                return false;


                case "food-images":
    // POST /api/food-images/upload
    return post
            && seg.length == 3
            && seg[2].equals("upload")
            && role.equals("STORE");

            case "orders":
                // POST /api/orders
                if (post && seg.length == 2) {
                    return role.equals("STUDENT");
                }

                // GET /api/orders/store/{storeId}
                if (get && seg.length == 4 && seg[2].equals("store")) {
                    return role.equals("STORE") && sameId(seg[3], ref);
                }

                // DELETE /api/orders/store/{storeId}/selected
if (delete && seg.length == 5
        && seg[2].equals("store")
        && seg[4].equals("selected")) {
    return role.equals("STORE") && sameId(seg[3], ref);
}

                // DELETE /api/orders/student/{studentId}/selected
                if (delete && seg.length == 5
                        && seg[2].equals("student")
                        && seg[4].equals("selected")) {
                    return role.equals("STUDENT") && sameId(seg[3], ref);
                }


                // GET /api/orders/student/{studentId}
                if (get && seg.length == 4 && seg[2].equals("student")) {
                    return role.equals("STUDENT") && sameId(seg[3], ref);
                }

                // PUT /api/orders/{orderId}/store/{storeId}/status
                if (put && seg.length == 6
                        && seg[3].equals("store") && seg[5].equals("status")) {
                    return role.equals("STORE") && sameId(seg[4], ref);
                }

                // PUT /api/orders/{orderId}/student/{studentId}/cancel
                if (put && seg.length == 6
                        && seg[3].equals("student") && seg[5].equals("cancel")) {
                    return role.equals("STUDENT") && sameId(seg[4], ref);
                }

                return false;

            
            
            case "notifications":
                if (!role.equals("STUDENT") && !role.equals("STORE")) {
                    return false;
                }

                // GET /api/notifications
                if (get && seg.length == 2) {
                    return true;
                }

                // GET /api/notifications/unread-count
                if (get && seg.length == 3
                        && seg[2].equals("unread-count")) {
                    return true;
                }

                // PUT /api/notifications/read-all
                if (put && seg.length == 3
                        && seg[2].equals("read-all")) {
                    return true;
                }

                // DELETE /api/notifications/selected
                if (delete && seg.length == 3
                        && seg[2].equals("selected")) {
                    return true;
                }

                
                // PUT /api/notifications/{id}/read
                return put && seg.length == 4
                        && seg[3].equals("read");

<<<<<<< HEAD
                                      case "admin":
                   return role.equals("ADMIN"); 

=======


               case "admin":
                   return role.equals("ADMIN");
                   
>>>>>>> 39af2dee2bb5014edad4d391b58ae5ec0de7bdb9
            default:
                return false;
        }
    }

    private boolean sameId(String text, Integer expected) {


        try {
            return expected != null && Integer.parseInt(text) == expected;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String[] segments(String uri) {

        String path = uri == null ? "" : uri;

        while (path.startsWith("/")) {
            path = path.substring(1);
        }

        while (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        return path.isEmpty() ? new String[0] : path.split("/");
    }

    private void reject(HttpServletResponse response, int status, String message)
            throws Exception {

        response.setStatus(status);
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write(message);
    }
}
