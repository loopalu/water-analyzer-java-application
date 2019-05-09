package main.proov;

import java.sql.*;

public class DatabaseRunner {

    public static void main(String[] args) {
        try {
            Class.forName("org.postgresql.Driver");
        }
        catch (java.lang.ClassNotFoundException e) {
            System.out.println("error 1");
            System.out.println(e.getMessage());
        }

        String url = "jdbc:postgresql://localhost:5432/postgres";
        String username = "postgres";
        String password = "aivar";

        try {
            Connection db = DriverManager.getConnection(url, username, password);
            Statement st = db.createStatement();
            ResultSet set = st.getGeneratedKeys();
            st.executeUpdate("INSERT INTO operaatorid(operaatori_eesnimi, operaatori_perekonnamini) " + "VALUES ('Uus', 'Tootaja')");
//            ResultSet rs = st.executeQuery("SELECT * FROM operaatorid;");
//            while (rs.next()) {
//                System.out.println(rs.getString(2)+ " " + rs.getString(3));
//            }
//            rs.close();
            st.close();
        }
        catch (java.sql.SQLException e) {
            System.out.println("error 2");
            System.out.println(e.getMessage());
        }
    }

}