package main;

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

        String url = "jdbc:postgresql://balarama.db.elephantsql.com:5432/tlhucqfs";
        String username = "tlhucqfs";
        String password = "pNLvnwhl0mh6ALiQrerX1y0sOuwhWB1h";

        try {
            Connection db = DriverManager.getConnection(url, username, password);
            Statement st = db.createStatement();
            ResultSet set = st.getGeneratedKeys();
//            ResultSet rs = st.executeQuery("SELECT * FROM operaatorid;");
            st.executeUpdate("INSERT INTO operaatorid(operaatori_eesnimi, operaatori_perekonnamini) " + "VALUES ('Uus', 'Tootaja')");
//            while (rs.next()) {
//                System.out.println(rs.getString(2));
//                System.out.println(rs.getString(3));
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