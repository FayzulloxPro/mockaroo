package dev.jlkesh.java_telegram_bots.config;

import dev.jlkesh.java_telegram_bots.daos.UserDao;
import dev.jlkesh.java_telegram_bots.dataserver.FakerApplicationGenerateRequest;
import dev.jlkesh.java_telegram_bots.dataserver.Field;
import dev.jlkesh.java_telegram_bots.dtos.FieldDTO;
import dev.jlkesh.java_telegram_bots.handlers.CallbackHandler;
import dev.jlkesh.java_telegram_bots.handlers.ClearHandler;
import dev.jlkesh.java_telegram_bots.handlers.Handler;
import dev.jlkesh.java_telegram_bots.handlers.MessageHandler;
import dev.jlkesh.java_telegram_bots.processors.GenerateDataCallbackProcessor;
import dev.jlkesh.java_telegram_bots.processors.Processor;
import dev.jlkesh.java_telegram_bots.processors.RegisterUserCallbackProcessor;
import dev.jlkesh.java_telegram_bots.services.UserService;
import dev.jlkesh.java_telegram_bots.steps.State;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadSafeBeansContainer {
    public static final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    public static final ThreadLocal<Handler> messageHandler = ThreadLocal.withInitial(MessageHandler :: new);
    public static final ThreadLocal<Handler> callbackHandler = ThreadLocal.withInitial(CallbackHandler :: new);
    public static final ThreadLocal<Handler> clearHandler = ThreadLocal.withInitial(ClearHandler :: new);
    public static final ConcurrentHashMap<Object, State> userState = new ConcurrentHashMap<>();
    public static final ThreadLocal<UserDao> userDao = ThreadLocal.withInitial(UserDao :: new);
    public static final ThreadLocal<UserService> userService = ThreadLocal.withInitial(() -> new UserService(userDao.get()));
    public static final ThreadLocal<RegisterUserCallbackProcessor> registerUserCallbackProcessor = ThreadLocal.withInitial(RegisterUserCallbackProcessor :: new);
    public static final ThreadLocal<GenerateDataCallbackProcessor> generateDataProcessor = ThreadLocal.withInitial(GenerateDataCallbackProcessor :: new);

    public static final ConcurrentHashMap<String, String> usernames=new ConcurrentHashMap<>();

    public static final ConcurrentHashMap<Long, FakerApplicationGenerateRequest> userDataRequests=new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, FieldDTO> userDataFields=new ConcurrentHashMap<>();
}
