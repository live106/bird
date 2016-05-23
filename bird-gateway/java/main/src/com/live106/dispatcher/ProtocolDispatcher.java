package com.live106.dispatcher;

import com.live106.message.Protocols;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by live106 on 2016/5/20.
 */
public final class ProtocolDispatcher {

    private static Map<Method, Method> has2GetMethods = new HashMap<>();
    private static Map<Method, Method> has2HandleMethods = new HashMap<>();

    static {
        Method[] methods = Protocols.Protocol.class.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().startsWith("has") && method.getReturnType().getName().equals("boolean")) {
                try {
                    Method getMethod = Protocols.Protocol.class.getMethod(method.getName().replace("has", "get"));
                    Method handleMethod = ProtocolDispatcher.class.getDeclaredMethod("handle", getMethod.getReturnType());
                    has2GetMethods.put(method, getMethod);
                    has2HandleMethods.put(method, handleMethod);
                } catch (NoSuchMethodException e) {
                    System.err.println(String.format("no get or handle method for [%20s]", method.getName().replace("has", "")));
                }
            }
        }
    }

    public static boolean dispatch(final Protocols.Protocol protocol) {
        has2GetMethods.entrySet().stream().filter(entry -> {
            try {
                return has2HandleMethods.containsKey(entry.getKey()) && (boolean) entry.getKey().invoke(protocol);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }).forEach(entry -> {
            try {
                Object subProtocol = entry.getValue().invoke(protocol);
                has2HandleMethods.get(entry.getKey()).invoke(null, subProtocol);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return false;
    }

    private static void handle(Protocols.CLogin protocol) {
        new ProtocolHandler<Protocols.CLogin>(protocol) {
            @Override
            public boolean handle() {
                System.err.println("handle login @ " + Thread.currentThread());
                return false;
            }
        }.doIt();
    }

    private static void handle(Protocols.CLogout protocol) {
        new ProtocolHandler<Protocols.CLogout>(protocol) {
            @Override
            public boolean handle() {
                System.err.println("handle logout @ " + Thread.currentThread());
                return false;
            }
        }.doIt();
    }

    public static void main(String[] args) {
        Protocols.Protocol.Builder builder = Protocols.Protocol.newBuilder();
        builder.setCLogin(Protocols.CLogin.newBuilder().setMid("1234"));
        builder.setCLogout(Protocols.CLogout.newBuilder().setUid(1L));
        ProtocolDispatcher.dispatch(builder.build());
    }
}
