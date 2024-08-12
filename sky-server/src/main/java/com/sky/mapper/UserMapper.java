package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.Map;

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
	User getById(Long userId);

	/**
	 * 统计注册时间处于某个时间段内的用户数量
	 * @param map
	 * @return
	 */
	Integer countByMap(Map map);
}
