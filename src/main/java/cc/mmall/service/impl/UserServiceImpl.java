package cc.mmall.service.impl;

import cc.mmall.common.Const;
import cc.mmall.common.ServerResponse;
import cc.mmall.common.TokenCache;
import cc.mmall.dao.UserMapper;
import cc.mmall.pojo.User;
import cc.mmall.service.IUserService;
import cc.mmall.util.MD5Util;
import com.sun.org.apache.regexp.internal.RE;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * Created by Administrator on 2017/6/4.
 */
@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            return ServerResponse.createByErrorMessage("用户名或密码不能为空");
        }
        // 判断用户名是否存在
        ServerResponse validResponse = this.checkValid(username, Const.USERNAME);
        if (validResponse.isSuccess()) {
            return ServerResponse.createByErrorMessage("用户名不存在");
        }
        // 登录
        String MD5Password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username, MD5Password);
        if (user == null) {
            return ServerResponse.createByErrorMessage("用户名或密码错误");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登录成功", user);
    }

    @Override
    public ServerResponse register(User user) {
        if (StringUtils.isBlank(user.getUsername()) || StringUtils.isBlank(user.getEmail()) || StringUtils.isBlank(user.getPassword())) {
            return ServerResponse.createByErrorMessage("请将注册信息填写完整");
        }
        ServerResponse validResponse = checkValid(user.getUsername(), Const.USERNAME);
        if (!validResponse.isSuccess()) {
            return validResponse;
        }
        validResponse = checkValid(user.getUsername(), Const.EMAIL);
        if (!validResponse.isSuccess()) {
            return validResponse;
        }
        user.setRole(Const.Role.ROLE_CUSTOMER);
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        int resultCount = userMapper.insert(user);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("注册失败");
        }
        return ServerResponse.createBySuccessMessage("注册成功");
    }

    public ServerResponse checkValid(String str, String type) {
        if (StringUtils.isBlank(str) || StringUtils.isBlank(type)) {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        if (Const.USERNAME.equals(type)) {
            int resultCount = userMapper.chkUsername(str);
            if (resultCount > 0) {
                return ServerResponse.createByErrorMessage("用户名已存在");
            }
        }
        if (Const.EMAIL.equals(type)) {
            int resultCount = userMapper.chkEmail(str);
            if (resultCount > 0) {
                return ServerResponse.createByErrorMessage("邮箱已存在");
            }
        }
        return ServerResponse.createBySuccessMessage("校验成功");
    }

    public ServerResponse<String> selectQuestion(String username) {
        ServerResponse validResponse = this.checkValid(username, Const.USERNAME);
        if (validResponse.isSuccess()) {
            return ServerResponse.createByErrorMessage("用户名不存在");
        }
        User user = userMapper.selectByUsername(username);
        String question = user.getQuestion();
        if (StringUtils.isBlank(question)) {
            return ServerResponse.createByErrorMessage("找回密码的问题为空");
        }
        return ServerResponse.createBySuccess(question);
    }

    @Override
    public ServerResponse<String> checkAnswer(String username, String answer) {
        if (StringUtils.isBlank(answer)) {
            return ServerResponse.createByErrorMessage("问题或答题为空");
        }
        ServerResponse validResponse = this.checkValid(username, Const.USERNAME);
        if (validResponse.isSuccess()) {
            return ServerResponse.createByErrorMessage("用户名不存在");
        }
        User user = userMapper.selectByUsername(username);
        if (!answer.equals(user.getAnswer())) {
            return ServerResponse.createByErrorMessage("找回密码答案不正确");
        }
        String forgetToken = UUID.randomUUID().toString();
        TokenCache.setKey(TokenCache.TOKEN_PREFIX + username, forgetToken);
        return ServerResponse.createBySuccess(forgetToken);
    }

    public ServerResponse forgetResetPassword(String username, String token, String newPassword) {
        if (StringUtils.isBlank(token) || StringUtils.isBlank(newPassword)) {
            return ServerResponse.createByErrorMessage("参数缺失");
        }
        ServerResponse validResponse = this.checkValid(username, Const.USERNAME);
        if (validResponse.isSuccess()) {
            return ServerResponse.createByErrorMessage("用户名不存在");
        }
        String cacheToken = TokenCache.getKey(TokenCache.TOKEN_PREFIX + username);
        if (StringUtils.isBlank(cacheToken)) {
            return ServerResponse.createByErrorMessage("token无效或过期");
        }
        if (StringUtils.equals(cacheToken, token)) {
            // 修改密码
            String MD5Password = MD5Util.MD5EncodeUtf8(newPassword);
            int resultCount = userMapper.updatePasswordByUsername(username, MD5Password);
            if (resultCount > 0) {
                return ServerResponse.createBySuccessMessage("修改密码成功");
            } else {
                return ServerResponse.createByErrorMessage("修改密码失败");
            }
        } else {
            return ServerResponse.createByErrorMessage("token错误");
        }
    }

    public ServerResponse resetPassword(String passwordOld, String passwordNew, User user) {
        if (StringUtils.isBlank(passwordNew) || StringUtils.isBlank(passwordOld)) {
            ServerResponse.createByErrorMessage("新老密码不能为空");
        }
        String MD5PasswordOld = MD5Util.MD5EncodeUtf8(passwordOld);
        int resultCount = userMapper.checkPassword(user.getId(), MD5PasswordOld);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("原密码不正确");
        }
        String MD5PasswordNew = MD5Util.MD5EncodeUtf8(passwordNew);
        resultCount = userMapper.updatePasswordByUsername(user.getUsername(), MD5PasswordNew);
        if (resultCount > 0) {
            return ServerResponse.createBySuccessMessage("修改密码成功");
        } else {
            return ServerResponse.createByErrorMessage("修改密码失败");
        }
    }

    public ServerResponse<User> updateInformation(User user) {
        // 用户名不能被更新
        // email需要校验
        if (StringUtils.isNotBlank(user.getEmail())) {
            int resultCount = userMapper.checkEmailById(user.getId(), user.getEmail());
            if (resultCount > 0){
                return ServerResponse.createByErrorMessage("邮箱已被占用");
            }
        }
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());
        updateUser.setPhone(user.getPhone());
        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if (updateCount == 0){
            return ServerResponse.createByErrorMessage("更新失败");
        }
        return ServerResponse.createBySuccess("更新成功",user);
    }

    public ServerResponse<User> getInformation(int userId){
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null){
            return ServerResponse.createByErrorMessage("找不到当前用户");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }


    // backend

    public ServerResponse checkAdminRole(User user){
        if (user != null && user.getRole() == Const.Role.ROLE_ADMIN){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }




}
