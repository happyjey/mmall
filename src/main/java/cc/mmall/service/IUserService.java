package cc.mmall.service;

import cc.mmall.common.ServerResponse;
import cc.mmall.pojo.User;

/**
 * Created by Administrator on 2017/6/4.
 */
public interface IUserService {
    ServerResponse<User> login(String username, String password);
    ServerResponse register(User user);
    ServerResponse checkValid(String str, String type);
    ServerResponse<String> selectQuestion(String username);
    ServerResponse<String> checkAnswer(String username,String answer);
    ServerResponse forgetResetPassword(String username, String token, String newPassword);
    ServerResponse resetPassword(String passwordOld, String passwordNew, User user);
    ServerResponse<User> updateInformation(User user);
    ServerResponse<User> getInformation(int userId);
    ServerResponse checkAdminRole(User user);


}
