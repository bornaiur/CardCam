package com.dongyang.pjw.cardcam;

/**
 * Created by borna on 2017-12-13.
 */

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by juneriu on 2017-12-13.
 */

public class CardParser {
    String name = "";
    String phonenumber = "";
    String workNumber = "";
    String workFaxNumber = "";
    String Email = "";
    String addr = "";
    String postcode = "";
    String organization = "";
    String department = "";
    String title="";
    String workEmail="";
    static String[] email = {"e","E","email","Email","E-mail:","e-mail","e.","E.","E-Mail","e-Mail","E-mail","이메일"};//10
    static String[] phone = {"H.P","M","m","M.","m.","Mobile","mobile","phone","Phone","휴대전화","이동전화"};//9
    static  String[] fax = {"F","f","f.","F.","Fax","fax","FAX"};//6
    static String[] etc = {"Tel","tel","t","전화","T","대표전화"};
    static   String[] text = new String[50];
    static String[] addrr = {"시","도"};
    static  String[] phone2 = {"직","직통","직통번호"};
    static String[] jobtext = {"장","대리","사원"};
    static  String[] team = {"팀","부서","파트","담당","부","본부"};
    static  String[] home = {"com","net","kr"};
    static  String jum = "점";
    int z;
    static  String[] lastname = {"김", "이", "박", "최", "정", "강", "조", "윤", "장", "임", "한", "오", "서", "신", "권",
            "황", "안", "송", "전", "홍", "류", "고", "문", "양", "손", "배", "조", "백", "허", "유", "남", "심", "노",
            "정", "하", "곽", "성", "차", "주", "우", "구", "신", "임", "전", "민", "유", "류", "나", "진", "지", "엄",
            "채", "원", "천", "방", "공", "깅", "현", "함", "변", "염", "양", "변", "여", "주", "도", "소", "석", "선",
            "설", "마", "길", "주", "연", "방", "위", "표", "명", "기", "반", "라", "왕", "금"
    };

    static    String femail = "", ftel = "", fphone ="" , fhomep="", fname="", fcompany="", faddr="", ffax="", saddrnum ="", fhome = "",fphone2="";

    public CardParser(String fullStr) {

        int aaaaa= 1;


        int i = 0;
        int text2cnt = 0;
		/*
		 * 1. 입력
		 * 2. 나누기
		 * 3. 1차제거(불필요 기호)
		 * 4. 1차 체크
		 * 5. 1차 구분
		 * 6. 출력
		 */
        String firstText = fullStr;

        //1.입력
        //String firstText = "고하영 출판사업팀 / 과장 --LEARN 21 (주)런 이십일특별시 성동구 성수01로 113 (성수동 제강빌딩 7층) T. 02,557.0646 F. 02,3446,9551 M. 010.8422.5892 hyko@chungdahm.com www.learn21.co.kr ";
        //String firstText = "哙 (주)미래엔 06532 서울시 서초구 신반포로 321 321 Shinbanpo-ro, Seocho-gu, Seoul, 06532, Korea T 02 3475 4033 M 010 8795 5682 F 02 541 8150 E ksg5682 @ mirae-n.ocm 기서구 감정한 Seong-Gyun Kim 교육영업팀 | 과장 Education Sales Team | Manager Mirae N Better Content, Better Life ";
        //String firstText = "蒸 イ+ 囵비룡소 마케팅부 대리 유선화 민음사 출판 그룹 비료소 서울특별시 강남구 도산대로1길 62 강남출판문화센터 6층 (06027) 이동전화 010-2994-4015 | 팩스 02-515-2007 | 대표전화 02-515-2000 (내선 216) 이메일 sunayu@bir.co.kr | 홈페이지 www.bir.co.kr ";
        //String firstText = "11 한빛라이프 한빛에듀 03785 서울시 서대문구 연희로2길 62 한빛미디어(주) 3층 T 336-7114 F 336-7124 www.hanbit.co.kr 문윤식 차장 010-3205-1349 2128-8795 yunsik@hanbit.co.kr";
        //String firstText = "분명한 뜻이 담겨 있는 책 기획마케팅팀 여인영 10881 경기도 파주시 회동길 337-9(서패동) Tel (02)337-5252 Fax (031)947-5868 Mobile 010-4001-4178 직통 070-7525-4129 E-mail yiy0430@ddstone.com www.ddstone.com 뜨인돌어린이 DSL";
        //String firstText = "DIDIMDOL CO., LTD 안재홍 An Jae Hong 마케팅본부총괄팀 장 부장 (주) 디딤돌 교육 03972 서울시 마포구 월드컵북로 122(성산동) 청원 선와이즈 타워 8층 8F, Chungwon Sunwise Tower, 122, (Sungsan-dong) World Cup buk-ro, Mapo-gu, Seoul, 03972 KOREA 전화 02 3146 0012 FAX 02 325 1552 E-mail: jhahn@didimdol.com 휴대전화 010-2520-9588 CI";
        // String firstText = "2.2 교보문고 www.kyobobook.co.kr 07305 서울특별시 영등포구 영중로 15 타임스퀘어 멀티플렉스 2층 Tel : 1544-1900(대) Fax : 02-2678-0058 직통 : 02-2678-3501 H.P 010-4301-7923 E-mail: shimn1103@kyobobook.co.kr 한자 꿈을 키우는 세상 Kyobo 교보문고 영등포점 전문서적파트 파트장 황 신 심";
        //String firstText = "김세정 단행본 마케팅팀 사원 사회평론 | Bricks | Education (주)사회평론 03978 서울시 마포구 월드컵북로12길 1 7 사평 빌딩 2층 t 02.2191.1179 f 02.326.1626 m 010-3118-6467 email hoithoit@sapyoung.com www.sapyoung.com";
        //String firstText = "중증장애인 생산품시설 인쇄 / DM/ 펠트바느질놀이 / 임가공 장애인직업재활시설 조은세상 정신보건사회복지사/원장 박정식 02047 서울특별시 중랑구 동일로 140길 31-21 T 02 439 9593삘트 임가공), 4209인쇄DM) F 02 439 9594 M 010 4249 9432 E goodworld07@hanmail.net H www.goodworld03.com";
        //String firstText = "책읽는곰 www.bearbooks.co.kr 영업부 과장 전 지 훈 Sales Manager Jeon, Ji-Hoon 03973 서울시 마포구 월드컵부록12길 74 102호 #102, 74, World Cup bukro 12-gil, Mapo-gu, Seoul, 03973, Korea Tel 02-332-2672-3 직통 070-4213-2973 Fax 02-338-2672 Mobile 010-4740-6382 Email jh@bearbooks.co.kr";
        //String firstText = "김지은 te oongjini 단행본사업본부/단행본 마케팅RT 주식 회사 웅진씽크빅 10881 경기도 파주시 회동길 20 tel 031-956-7325 fax 031-949-0817 mobile 010-2668-2046 e-mail mojo713@wjtb.net ";
        //2. 나누기
        StringTokenizer st = new StringTokenizer(firstText);
        while(st.hasMoreElements()) {
            String result = st.nextElement().toString();
            text[i++] = result;
        }
        int a;
        String[] text2 = new String[i];
        int t1 = 0;
        //3. 1차 제거
        String[] deleteText = {",",".","/",":","|"};//4
        int b;
        int checker = 0;
        for(a = 0; a<i; a++) {
            checker = 0;
            for(b = 0; b<deleteText.length ;b++) {
                if(text[a].equals(deleteText[b])) {
                    checker = 1;
                }
            }
            if(checker==0) {
                text2[t1++] = text[a];
            }
        }
        //4. 1차 체크
        for(int re = 0; re<50; re++) {
            text[re] = " ";
        }
        int addrnumint = 0;
        text = resetarr(text);

        Pattern pattern = Pattern.compile("\\d{5}");
        Matcher matcher = pattern.matcher(firstText);
        while(matcher.find()) {
            saddrnum = matcher.group();
        }
        for(a=0;a<t1;a++) {

            if(text2[a].contains(saddrnum)) {
                if(!text2[a].equals(saddrnum)) {
                    text2[a] = saddrnum;
                    //System.out.println(text2[a]);
                }
            }

        }


        for(a = 0; a<t1;a++) {
            text2cnt = fisrtCheck(email, text2, a, text2cnt,"email") ;
            text2cnt = fisrtCheck(phone, text2, a, text2cnt,"phone") ;
            text2cnt = fisrtCheck(phone2, text2, a, text2cnt,"phone2") ;
            text2cnt = fisrtCheck(fax, text2, a, text2cnt,"fax") ;
            text2cnt = fisrtCheck(etc, text2, a, text2cnt,"etc") ;

            if(text2[a].equals(saddrnum)) {
                text[a] = "2";
                addrnumint = a;
                text2cnt++;
            }
            for(b=0;b<addrr.length;b++) {
                if(text2[a].endsWith(addrr[b])) {
                    text[a] = "3";
                }
            }
        }
        if(femail.equals("")) {
            for(a = 0; a<t1;a++) {
                if(text2[a].contains("@")) {
                    femail = text2[a];
                    text[a] = "1";
                }
            }
        }

        for(b = 0; b < text2.length;b++) {

            if(text[b].equals("2") || text[b].equals("3")) {
                break;
            }

        }

		/*for(a = 0;a<text2.length;a++) {
			System.out.print(text2[a]);
			System.out.print("---");
			System.out.println(text[a]);
		}
*/

        //5. 1차 판별
        //System.out.println(b);
        int checkaddr = b;

        if(text[b].equals("2")) {
            //우편번호 + 주
            checkaddr++;
            while(!text[checkaddr].equals("1")) {
                faddr = faddr + " " + text2[checkaddr];
                text[checkaddr++] = "4";

            }


        }else if(text[b].equals("3")) {
            //주소+우편번호
            while(text[checkaddr].equals("2")) {
                faddr = faddr + " " + text2[checkaddr++];
                text[checkaddr] = "4";
            }

        }else {}

        System.out.print("전화번호 : ");
        System.out.println(fphone);

        System.out.print("회사전화번호 : ");
        System.out.println(ftel);
        System.out.print("회사팩스번호 : ");
        System.out.println(ffax);
        System.out.print("이메일 : ");
        System.out.println(femail);
        System.out.print("주소 : ");
        System.out.println(faddr);
        System.out.print("우편번호 : ");
        System.out.println(saddrnum);


        //6. 1차 제거
        String[] text3 = new String[t1-text2cnt-1];
        b = 0;
        for(a = 0; a<t1; a++) {
            if(text[a].equals("0"))
                text3[b++] = text2[a];
        }

        text = resetarr(text);

        String job = "";

        for(a = 0; a<b;a++) {
            if(text3[a].endsWith(jum)) {
                text[a] = "1";
                job = text3[a];
            }

        }
        for(a = 0; a<b;a++) {
            for(int aa=0;aa<home.length;aa++) {
                if(text3[a].endsWith(home[aa])) {
                    fhome=text3[a];
                    text[a] = "1";

                }
            }
        }
        boolean teamcheck = false;
        for(a = 0; a<b;a++) {
            for(z = 0; z<team.length;z++) {
                if(text3[a].contains(team[z])) {
                    text[a] = "1";
                    job = job + " " + text3[a];
                    teamcheck = true;
                    break;
                }
                if(teamcheck==true)
                    break;

            }
            for(z = 0; z<jobtext.length;z++) {
                if(text3[a].endsWith(jobtext[z])) {
                    text[a] = "1";
                    job = job + " " + text3[a];
                    break;
                }

            }


        }
        //----------------------------

        int c = 0;
        for(a = 0; a<b; a++) {
            if(!text[a].equals("1"))
                text2[c++] = text3[a];
        }
        text = resetarr(text);



        int t3cnt=0;
        for(a=0;a<c;a++) {
            //System.out.println(text3[a]);
            if(text2[a].length() == 1) {
                t3cnt++;
                text[a] = "1";
            }
        }

        if(t3cnt == 3 || t3cnt == 4) {

            for(a = 0; a < b; a++) {
                if(text[a].equals("1")) {
                    fname = fname+text2[a];
                }
            }
        }else {
            for(a = 0; a<c;a++) {
                if(text2[a].length()==3) {
                    for(int zz = 0; zz <lastname.length;zz++) {
                        if(text2[a].startsWith(lastname[zz])) {
                            fname = text2[a];
                            text[a] = "1";
                        }
                    }
                }
            }
        }
        System.out.print("홈페이지 : ");
        System.out.println(fhome);

        System.out.print("부서/직책 : ");
        System.out.println(job);


        System.out.print("이름 : ");
        System.out.println(fname);



        b = 0;

        for(a = 0; a<c; a++) {
            if(!text[a].equals("1")&& !text[a].equals("null")){
                b++;
            }
        }
        //b—;
        String[] text4 = new String[b];
        b = 0;
        for(a = 0; a<text4.length; a++) {
            if(!text[a].equals("1")&& !text[a].equals("null")){
                text4[b++] = text2[a];
            }
        }
        text = resetarr(text);
		/*for(a=0;a<text4.length;a++) {
			System.out.println(text4[a]);
		}*/
        //System.out.println(firstText);


        fcompany = compname(text4);
        System.out.print("회사명 : ");
        System.out.println(fcompany);
        System.out.println("—————————");



        name = fname;
        phonenumber = fphone;
        workNumber = fphone2;
        workFaxNumber = ffax;
        Email = femail;
        addr = faddr;
        postcode = saddrnum;
        organization = fcompany;
        department = job; // 부서 + 직위
        workEmail=fhome;






    }
    public static String compname(String[] list) {
        String text = "1";
        int a,b;
        String[] compe = {"(주)","[주]","주식회사","회사"};
        try {
            //1단계 - 중복체크
            for( a=0; a<list.length-1;a++) {
                for( b= a+1; b<list.length;b++) {
                    if(list[a].equals(list[b])) {
                        text = list[a];
                        return text;
                    }
                }
            }
        }catch(Exception e) {
            text = "1";
        }
        //———
        //2단계 - (주)text 확인
        try {
            for( a=0; a<list.length-1;a++) {
                for( b = 0; b<compe.length;b++) {
                    if(list[a].contains(compe[b]) && !list[a].equals(compe[b])) {
                        return list[a];
                    }
                }
            }
        }catch(Exception e) {
            text = "1";
        }

        //———
        //3단계 - (주) text 확인
        try {
            for( a=0; a<list.length-1;a++) {
                for( b = 0; b<compe.length;b++) {
                    if(list[a].equals(compe[b])) {
                        return "이거 둘중에 선택 = " + list[a-1] + " " + list[a+1];
                    }
                }
            }
        }catch(Exception e) {
            text = "1";
        }

        if(text.equals("1"))
            text = "직접입력";
        return text;
    }

    public static String[] resetarr (String[] list) {

        for(int resetnum = 0; resetnum<list.length;resetnum++) {
            list[resetnum] = "0";
        }

        return list;
    }

    public static int fisrtCheck(String[] target, String[] base, int inputnum, int cnt,String type) {

        for(int b=0;b<target.length;b++) {
            if(base[inputnum].equals(target[b])) {
                switch(type) {
                    case "email":
                        femail=base[inputnum+1];
                        break;
                    case "phone":
                        fphone=base[inputnum+1];
                        break;
                    case "phone2":
                        fphone2=base[inputnum+1];
                        break;
                    case "comp":
                        fcompany=base[inputnum+1];
                        break;
                    case "fax":
                        ffax=base[inputnum+1];
                        break;
                    case "etc":
                        ftel = base[inputnum+1];
                        break;

                }
                text[inputnum] = "1";
                text[inputnum+1] = "1";
                cnt++;
                break;
            }
        }
        return cnt;
    }


    public String getName() {
        return name;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public String getWorkNumber() {
        return workNumber;
    }

    public String getWorkFaxNumber() {
        return workFaxNumber;
    }

    public String getEmail() {
        return Email;
    }

    public String getAddr() {
        return addr;
    }

    public String getPostcode() {
        return postcode;
    }

    public String getOrganization() {
        return organization;
    }

    public String getDepartment() {
        return department;
    }

    public String getTitle() {
        return title;
    }

    public String getWorkEmail() {
        return workEmail;
    }
}
