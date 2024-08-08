package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
	/**
	 *  根据openid查询用户
	 * @param openid
	 * @return
	 */
	@Select("select * from user where openid = #{openid}")
	User getByOpenid(String openid);

	/**
	 *  创建对象
	 * @param user
	 */
	void insert(User user);

	/**
	 *  根据id获取用户
	 * @param userId
	 * @return
	 */
	@Select("select * from user where id = #{id}")
	User getByid(Long userId);
}
