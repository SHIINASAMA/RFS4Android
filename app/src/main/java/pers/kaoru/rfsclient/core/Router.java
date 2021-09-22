package pers.kaoru.rfsclient.core;

import java.util.Stack;

public class Router {

    private final Stack<String> stack;

    public Router() {
        stack = new Stack<>();
    }

    public void enter(String director) {
        if (director.equals("/")) return;
        stack.push(director);
    }

    public boolean back() {
        if (!stack.isEmpty()) {
            stack.pop();
            return true;
        }
        return false;
    }

    public String preback() {
        if (!stack.isEmpty()) {
            int count = stack.size(); // 1 - n
            int pos = 0;

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("/");
            for (String directory : stack) {
                if (pos == count - 1) {
                    return stringBuilder.toString();
                }
                pos += 1;
                stringBuilder.append(directory).append('/');
            }
        }
        return null;
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public String current() {
        return stack.lastElement();
    }

    public void reset(String path) {
        String[] dirs = path.split("/");
        stack.clear();
        for (String dir : dirs) {
            if (dir.isEmpty() || dir.equals("/")) continue;
            stack.push(dir);
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("/");
        for (String directory : stack) {
            stringBuilder.append(directory).append('/');
        }
        return stringBuilder.toString();
    }
}
