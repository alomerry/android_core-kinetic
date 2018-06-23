package mo.database.model;

public class Teacher {


    private int teacherId;
    private String teacherName_CN;
    private String teacherName_EN;
    private String department;//系
    private String job;//职位
    private int teacherRoom;
    private String picture;//图片路径

    public Teacher(int teacherId, String teacherName_CN, String teacherName_EN, String department, String job, int teacherRoom, String picture) {
        this.teacherId = teacherId;
        this.teacherName_CN = teacherName_CN;
        this.teacherName_EN = teacherName_EN;
        this.department = department;
        this.job = job;
        this.teacherRoom = teacherRoom;
        this.picture = picture;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    public String getTeacherName_CN() {
        return teacherName_CN;
    }

    public void setTeacherName_CN(String teacherName_CN) {
        this.teacherName_CN = teacherName_CN;
    }

    public int getTeacherRoom() {
        return teacherRoom;
    }

    public void setTeacherRoom(int teacherRoom) {
        this.teacherRoom = teacherRoom;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getTeacherName_EN() {
        return teacherName_EN;
    }

    public void setTeacherName_EN(String teacherName_EN) {
        this.teacherName_EN = teacherName_EN;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }
}
