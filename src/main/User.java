package main;

/**
 * Class for User object. User is the user of desktop application.
 */
public class User {
    private String name;
    private int userClass;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getUserClass() {
        return userClass;
    }

    public void setUserClass(int userClass) {
        this.userClass = userClass;
    }
}
