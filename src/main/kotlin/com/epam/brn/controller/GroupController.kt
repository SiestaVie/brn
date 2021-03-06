package com.epam.brn.controller

import com.epam.brn.dto.BaseResponseDto
import com.epam.brn.dto.BaseSingleObjectResponseDto
import com.epam.brn.dto.ExerciseGroupDto
import com.epam.brn.service.ExerciseGroupsService
import com.epam.brn.service.LocalePostprocessor
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/groups")
@Api(value = "/groups", description = "Contains actions over groups")
class GroupController(val exerciseGroupsService: ExerciseGroupsService, val localePostProcessor: LocalePostprocessor<ExerciseGroupDto>) {

    // The discrepancy in naming with "Groups" endpoint and "ExerciseGroup" entity is due to
    // group being a reserved word in db.
    @GetMapping
    @ApiOperation("Get all groups")
    fun getAllGroups(): ResponseEntity<BaseResponseDto> {
        val data = exerciseGroupsService.findAllGroups()
            .map { localePostProcessor.postprocess(it) }
        return ResponseEntity.ok().body(BaseResponseDto(data = data))
    }

    @GetMapping(value = ["/{groupId}"])
    @ApiOperation("Get group by id")
    fun getGroupById(@PathVariable("groupId") groupId: Long): ResponseEntity<BaseSingleObjectResponseDto> {
        return ResponseEntity.ok()
            .body(BaseSingleObjectResponseDto(data = exerciseGroupsService.findGroupDtoById(groupId)))
    }
}
