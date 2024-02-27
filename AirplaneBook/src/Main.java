import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/study_db?useSSL=false";
        String account = "study";
        String password = "1234";
        Scanner sc = new Scanner(System.in);
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, account, password);
            PreparedStatement pstmt = null;
            ResultSet rs = null;

            int mainMenu = 0;
            while (mainMenu != 4) {
                System.out.print("===================메인  메뉴========================\n" +
                        "1.회원가입 2.로그인 3.관리자 로그인 4.종료 \n" +
                        "====================================================\n" +
                        "메뉴를 선택하시오 >> ");
                mainMenu = Integer.parseInt(sc.nextLine());
                switch (mainMenu) {
                    case 1:
                        signUp(conn);
                        break;
                    case 2:
                        login(conn);
                        break;
                    case 3:
                        loginAdmin(conn);
                        break;
                    case 4:
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void signUp(Connection conn) { //회원 가입
        Scanner sc = new Scanner(System.in);
        String sql = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        boolean existID = true;
        String custID = null;
        try {
            System.out.println("----------------------회원 가입----------------------");
            while (existID) { //ID중복체크
                System.out.print("ID를 입력하세요: ");
                custID = sc.nextLine().trim();
                sql = "SELECT CUSTID FROM CUSTOMER WHERE CUSTID = ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, custID);
                rs = pstmt.executeQuery();
                if (rs.next()) {
                    System.out.println("이미 존재하는 ID입니다 다시 입력해주세요.");
                } else {
                    existID = false;
                }
            }
            System.out.print("비밀번호를 입력하세요: ");
            String pwd = sc.nextLine().trim();

            System.out.print("이름을 입력하세요: ");
            String name = sc.nextLine().trim();

            System.out.print("여권번호를 입력하세요: ");
            String passportNo = sc.nextLine().trim();

            System.out.print("성별을 입력하세요: ");
            String sex = sc.nextLine().trim();

            sql = "INSERT INTO CUSTOMER VALUES (?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, custID);
            pstmt.setString(2, pwd);
            pstmt.setString(3, name);
            pstmt.setString(4, passportNo);
            pstmt.setString(5, sex);
            int i = pstmt.executeUpdate();
            System.out.println("회원가입이 완료되었습니다! 로그인 해주세요");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    public static void login(Connection conn){ //로그인
        Scanner sc = new Scanner(System.in);
        String sql = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String id;
        String pwd;
        try{
            System.out.println("--------------------로그인--------------------");
            System.out.print("ID: ");
            id = sc.nextLine().trim();
            System.out.print("PassWord: ");
            pwd = sc.nextLine().trim();
            sql = "SELECT * FROM CUSTOMER WHERE CUSTID = ? and PWD = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1,id);
            pstmt.setString(2,pwd);
            rs = pstmt.executeQuery();
            if(rs.next()){
                System.out.printf("%s %s %s %s %s\n",
                        rs.getString(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getString(5));
                loginMenu(conn, id);
            }else{
                System.out.println("ID 혹은 PassWord가 잘못 입력 되었습니다.");
            }

        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
    public static void loginMenu(Connection conn, String id){
        Scanner sc = new Scanner(System.in);
        try{
            int logMenu = 0;
            while (logMenu!=5){
                System.out.print("===================로그인  메뉴========================\n" +
                        "1.예약 2.예약 내역 조회 3.예약 변경 4.예약 취소 5.로그 아웃 \n" +
                        "====================================================\n" +
                        "메뉴를 선택하시오 >> ");
                logMenu = Integer.parseInt(sc.nextLine());
                switch (logMenu){
                    case 1:
                        Booking(conn, id);
                        break;
                    case 2:
                        showAllBook(conn, id);
                        break;
                    case 3:
                        changeBook(conn, id);
                        break;
                    case 4:
                        cancelBook(conn, id);
                        break;
                    case 5:
                        break;
                }//스위치
            }//로그인 while문
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }//로그인 메뉴
    public static void Booking(Connection conn, String id){ // 예약하기
        Scanner sc = new Scanner(System.in);
        String sql = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            showAllFlight(conn);
            LocalDateTime currentDateTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            String formattedDateTime = currentDateTime.format(formatter);
            System.out.print("운항 번호를 선택해주세요.\n>>");
            int flightNumber = sc.nextInt();
            sc.nextLine(); // 버퍼 비우기

            // Check if the user has already booked this flight
            sql = "SELECT COUNT(*) FROM BOOK WHERE FLIID = ? AND CUSTID = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, flightNumber);
            pstmt.setString(2, id);
            rs = pstmt.executeQuery();
            int existingBookings = 0;
            if(rs.next()){
                existingBookings = rs.getInt(1);
            }

            // 운항 번호에 해당하는 운항 정보 가져오기
            sql = "SELECT SEATSLEFT, PRICE FROM FLIGHT WHERE FliID = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, flightNumber);
            rs = pstmt.executeQuery();
            int seatsLeft = 0;
            int price = 0;
            if(rs.next()){
                seatsLeft = rs.getInt(1);
                price = rs.getInt(2);
            }

            if(existingBookings == 0 && seatsLeft > 0){ // 사용자가 해당 운항 번호로 예약을 하지 않았고, 잔여 좌석이 있는 경우에만 예약 가능
                sql = "INSERT INTO BOOK (BOOKID, FLIID, CUSTID, PRICE) VALUES (?, ?, ?, ?)";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, formattedDateTime);
                pstmt.setInt(2, flightNumber);
                pstmt.setString(3, id);
                pstmt.setInt(4, price);
                int i = pstmt.executeUpdate();
                System.out.println(i+"건이 예약되었습니다.");

                // 잔여 좌석 수 감소
                sql = "UPDATE FLIGHT SET SEATSLEFT = ? WHERE FliID = ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, seatsLeft - 1);
                pstmt.setInt(2, flightNumber);
                pstmt.executeUpdate();
            } else {
                System.out.println("이미 예약된 운항 번호이거나 더 이상 예약이 불가능한 항공편입니다.");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }// 예약하기
    public static void showAllBook(Connection conn, String id){ // 예약 내역 조회
        Scanner sc = new Scanner(System.in);
        String sql = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            sql = "SELECT * FROM BOOK WHERE CUSTID = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            rs = pstmt.executeQuery();
            boolean hasBook = rs.next();
            System.out.println("----------\""+id+"\"님의 예약 정보----------\n" +
                    "    예약 번호   |  운항 번호  |  가격  ");
            if (!hasBook) {
                System.out.println("      예약 정보가 없습니다.");
            } else {
                do {
                    System.out.printf("%s |     %-3d   |  %d  \n", rs.getString(1), rs.getInt(2),rs.getInt(4));
                } while (rs.next());
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }// 예약 내역 조회
    public static void changeBook(Connection conn, String id){
        Scanner sc = new Scanner(System.in);
        String sql = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            System.out.println("취소할 운항 번호를 입력해주세요.");
            int oldFlightID = Integer.parseInt(sc.nextLine().trim());
            System.out.println("다시 예약할 운항 번호를 입력해주세요");
            int newFlightID = Integer.parseInt(sc.nextLine().trim());

            int oldPrice = 0;
            int newPrice = 0;

            sql = "SELECT PRICE FROM FLIGHT WHERE FLIID = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, oldFlightID);
            rs = pstmt.executeQuery();
            if(rs.next()){
                oldPrice = rs.getInt(1);
            }
            sql = "SELECT PRICE FROM FLIGHT WHERE FLIID = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, newFlightID);
            rs = pstmt.executeQuery();
            if(rs.next()){
                newPrice = rs.getInt(1);
            }

            // Check if new flight ID is available for booking
            sql = "SELECT SEATSLEFT FROM FLIGHT WHERE FliID = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, newFlightID);
            rs = pstmt.executeQuery();
            int seatsLeft = 0;
            if(rs.next()){
                seatsLeft = rs.getInt(1);
            }

            if (seatsLeft > 0) { // If there are available seats, proceed with the change
                sql = "UPDATE BOOK SET BOOKID = ?, FLIID = ?, PRICE = ? WHERE CUSTID = ? AND FLIID = ?";
                pstmt = conn.prepareStatement(sql);
                LocalDateTime currentDateTime = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                String formattedDateTime = currentDateTime.format(formatter);
                pstmt.setString(1, formattedDateTime);
                pstmt.setString(4, id);
                pstmt.setInt(2, newFlightID);
                pstmt.setInt(3, newPrice);
                pstmt.setInt(5, oldFlightID);

                int i = pstmt.executeUpdate();
                int finalPrice =0;

                if(newPrice>oldPrice){
                    finalPrice = newPrice - oldPrice;
                    System.out.println(i+"건이 변경되었습니다.\n추가 금액은"+finalPrice+"원 입니다.");
                }else{
                    finalPrice = oldPrice - newPrice;
                    System.out.println(i+"건이 변경되었습니다.\n환불 금액은"+finalPrice+"원 입니다.");
                }


                // Increase seats for old flight
                sql = "UPDATE FLIGHT SET SEATSLEFT = SEATSLEFT + 1 WHERE FliID = ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, oldFlightID);

                int j = pstmt.executeUpdate();

                // Decrease seats for new flight
                sql = "UPDATE FLIGHT SET SEATSLEFT = SEATSLEFT - 1 WHERE FliID = ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, newFlightID);
                int k  = pstmt.executeUpdate();
            } else {
                System.out.println("새로운 운항 번호에 대한 예약이 불가능합니다.");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }//예약 변경


    public static void cancelBook(Connection conn, String id){//예약 취소
        Scanner sc = new Scanner(System.in);
        String sql = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            System.out.println("취소하실 운항 번호를 입력해주세요.");
            int flightNumber = Integer.parseInt(sc.nextLine().trim());

            // 운항 번호에 해당하는 운항 정보 가져오기
            sql = "SELECT SEATSLEFT FROM FLIGHT WHERE FliID = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, flightNumber);
            rs = pstmt.executeQuery();
            int seatsLeft = 0;
            if(rs.next()){
                seatsLeft = rs.getInt(1);
            }

            sql = "DELETE FROM BOOK WHERE CUSTID = ? AND FLIID = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.setInt(2, flightNumber);
            int i = pstmt.executeUpdate();
            System.out.println(i+"건이 취소되었습니다.");

            // Increase seats
            sql = "UPDATE FLIGHT SET SEATSLEFT = SEATSLEFT + 1 WHERE FliID = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, flightNumber);
            pstmt.executeUpdate();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }//예약 취소

    public static void loginAdmin(Connection conn) {
        Scanner sc = new Scanner(System.in);
        System.out.println("--------------------관리자 로그인--------------------");
        System.out.print("ID: ");
        String adminID = sc.nextLine().trim();
        System.out.print("PassWord: ");
        String adminPwd = sc.nextLine().trim();
        Admin admin = new Admin("Admin", "1234"); //ID, PassWord지정해서 생성자 생성
        if (admin.getAdminID().equals(adminID) && admin.getAdminPwd().equals(adminPwd)) {
            System.out.println("관리자로 로그인 되었습니다.");
            int menu = 0;
            while (menu != 4) {
                System.out.print("===================관리자  메뉴========================\n" +
                        "1.전체 운항 정보 조회 2.전체 고객 정보 조회 3.운항 정보 추가 4.관리자모드 종료 \n" +
                        "====================================================\n" +
                        "메뉴를 선택하시오 >> ");
                menu = Integer.parseInt(sc.nextLine().trim());
                switch (menu) {
                    case 1:
                        showAllFlight(conn);
                        break;
                    case 2:
                        showAllCustomer(conn);
                        break;
                    case 3: //운항 정보 추가
                        addNewFlight(conn);
                        break;
                    case 4:
                        System.out.println("관리자 모드 종료");
                        break;
                }
            }
        } else {
            System.out.println("ID 혹은 PassWord가 잘못 입력 되었습니다.");
        }

    }

    public static void showAllFlight(Connection conn) { //전체 운항 정보 조회
        String sql = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            sql = "SELECT * FROM FLIGHT";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();// rs.next() 호출 결과를 변수에 저장
            boolean hasData = rs.next();

            if (!hasData) {
                System.out.println("운항 정보가 없습니다.");
            } else {
                System.out.println("운항 번호 |   항공기 번호   |  출발지  |  목적지  |       출발 날짜/시간       |       도착 날짜/시간       | 잔여좌석  |  가격");
                do {
                    System.out.printf("   %-3d   |  %-10s  |  %-5s  |  %-5s  |  %-22s  |  %-22s  |  %-5d  |  %-5d\n", rs.getInt(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4),
                            rs.getString(5),
                            rs.getString(6),
                            rs.getInt(7),
                            rs.getInt(8));
                } while (rs.next());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }//showAllFlight

    public static void showAllCustomer(Connection conn) { //전체 고객 정보 조회
        String sql = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            sql = "SELECT * FROM customer";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();// rs.next() 호출 결과를 변수에 저장
            boolean hasData = rs.next();

            if (!hasData) {
                System.out.println("고객 정보가 없습니다.");
            } else {
                do {
                    System.out.printf("%s %s %s %s %s\n", rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5));
                } while (rs.next());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }//showAllCustomer
    public static void addNewFlight(Connection conn) { //운항 정보 추가
        Scanner sc = new Scanner(System.in);
        String sql = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            System.out.print("항공기 번호: ");
            String airid = sc.next();
            System.out.print("출발지: ");
            String origin = sc.next();
            System.out.print("도착지: ");
            String destination = sc.next();
            System.out.print("출발 날짜(yyyy-mm-dd HH-mm-ss): ");
            String dptdate = sc.next();
            System.out.print("도착 날짜(yyyy-mm-dd HH-mm-ss): ");
            String arrdate = sc.next();
            System.out.print("좌석 수: ");
            int seatsleft = sc.nextInt();
            System.out.print("가격: ");
            int price = sc.nextInt();

            sql = "INSERT INTO FLIGHT (AIRID, ORIGIN, DESTINATION, DPTDATE, ARRDATE, SEATSLEFT, PRICE) VALUES (?, ?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, airid);
            pstmt.setString(2, origin);
            pstmt.setString(3, destination);
            pstmt.setString(4, dptdate);
            pstmt.setString(5, arrdate);
            pstmt.setInt(6, seatsleft);
            pstmt.setInt(7, price);

            int i = pstmt.executeUpdate();
            System.out.println(i+"건이 추가되었습니다.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }//addNewFlight
}//Main
