package com.atguigu.lease.common.login;

public class LoginUserHolder {

    private static final ThreadLocal<LoginUser> userThreadLocal = new ThreadLocal<>();

    public static void setLoginUser(LoginUser loginUser) {
        userThreadLocal.set(loginUser);
    }

    public static LoginUser getLoginUser() {
        return userThreadLocal.get();
    }

    public static void clear() {
        userThreadLocal.remove();
    }
}