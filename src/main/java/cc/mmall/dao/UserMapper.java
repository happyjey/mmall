package cc.mmall.dao;

import cc.mmall.pojo.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    int chkUsername(String username);
    int chkEmail(String username);

    User selectLogin(@Param("username") String username, @Param("password") String password);

    User selectByUsername(String username);

    int updatePasswordByUsername(@Param("username") String username,@Param("newPassword") String newPassword);

    int checkPassword(@Param("id") int id,@Param("password") String password);

    int checkEmailById(@Param("id") int id,@Param("email") String email);
}