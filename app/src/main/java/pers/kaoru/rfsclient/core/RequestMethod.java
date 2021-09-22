package pers.kaoru.rfsclient.core;

public enum RequestMethod {
    // 常规本地文件操作
    LIST_SHOW,
    REMOVE,
    COPY,
    MOVE,
    MAKE_DIRECTORY,

    // 传输文件操作
    UPLOAD,
    DOWNLOAD,

    // 账号验证相关
    VERIFY,

    // 服务器校验用
    ERROR
}
