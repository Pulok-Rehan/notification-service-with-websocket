//package com.company.notification.websocket;
//
//import com.company.notification.security.JwtService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.lang.NonNull;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.MessageChannel;
//import org.springframework.messaging.simp.stomp.StompCommand;
//import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
//import org.springframework.messaging.support.ChannelInterceptor;
//import org.springframework.messaging.support.MessageHeaderAccessor;
//import org.springframework.stereotype.Component;
//
//import java.security.Principal;
//
///**
// * Extracts the JWT from the STOMP CONNECT frame's Authorization header, resolves the
// * mobileNumber, and sets it as the STOMP Principal so that /user/{mobile}/... destinations
// * and presence tracking work without re-authenticating every frame.
// */
//@Component
//@RequiredArgsConstructor
//public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {
//
//    private final JwtService jwtService;
//
//    @Override
//    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
//        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
//        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
//            String authHeader = accessor.getFirstNativeHeader("Authorization");
//            String token = (authHeader != null && authHeader.startsWith("Bearer "))
//                    ? authHeader.substring(7) : authHeader;
//            if (token != null && jwtService.isValid(token)) {
//                String mobile = jwtService.extractMobile(token);
//                accessor.setUser((Principal) () -> mobile);
//            }
//        }
//        return message;
//    }
//}
