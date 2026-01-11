package com.example.demo.board.mapper;

import com.example.demo.board.OperatorUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OperatorUserMapper {
	OperatorUser selectByUsername(@Param("username") String username);

	OperatorUser insert(OperatorUser operatorUser);
}
