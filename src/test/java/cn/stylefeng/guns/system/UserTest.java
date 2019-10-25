package cn.stylefeng.guns.system;

import cn.stylefeng.guns.modular.system.mapper.UserMapper;
import org.apache.commons.lang3.StringEscapeUtils;
import org.junit.Test;

import javax.annotation.Resource;

/**
 * 用户测试
 *
 * @author fengshuonan
 * @date 2017-04-27 17:05
 */
public class UserTest{

    @Resource
    UserMapper userMapper;

    @Test
    public void userTest() throws Exception {

        System.out.println(StringEscapeUtils.unescapeXml("&lt;svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:oryx=\"http://oryx-editor.org\""));
    }

}
