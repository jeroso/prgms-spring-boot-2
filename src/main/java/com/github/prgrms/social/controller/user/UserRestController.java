package com.github.prgrms.social.controller.user;

import com.github.prgrms.social.controller.ApiResult;
import com.github.prgrms.social.error.NotFoundException;
import com.github.prgrms.social.model.user.Email;
import com.github.prgrms.social.model.user.Role;
import com.github.prgrms.social.model.user.User;
import com.github.prgrms.social.security.Jwt;
import com.github.prgrms.social.security.JwtAuthentication;
import com.github.prgrms.social.service.user.UserService;
import com.sun.jdi.request.DuplicateRequestException;
import io.swagger.annotations.ApiParam;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.github.prgrms.social.controller.ApiResult.OK;
import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("api")
public class UserRestController {

  private final Jwt jwt;

  private final UserService userService;

  public UserRestController(Jwt jwt, UserService userService) {
    this.jwt = jwt;
    this.userService = userService;
  }

  @PostMapping(path = "user/exists")
  public ApiResult<Boolean> checkEmail(@RequestBody @ApiParam(value = "example:{\"email\":\"song@gmail.com\"}")Map<String, String> request) {
    // TODO 이메일 중복 확인 로직 구현
    Email email = new Email(request.get("email"));
    // BODY 예시: {
    //	"email": "iyboklee@gmail.com"
    //}
    return OK(userService.findByEmail(email).isPresent());
  }

  @PostMapping(path = "user/join")
  public ApiResult<JoinResult> join(@RequestBody JoinRequest joinRequest) {
    User user = userService.join(new Email(joinRequest.getPrincipal()), joinRequest.getCredentials());
    String apiToken = user.newApiToken(jwt, new String[]{Role.USER.value()});
    return OK(
      new JoinResult(apiToken, new UserDto(user))
    );
  }

  @GetMapping(path = "user/me")
  public ApiResult<UserDto> me(@AuthenticationPrincipal JwtAuthentication authentication) {
    return OK(
      userService.findById(authentication.id)
        .map(UserDto::new)
        .orElseThrow(() -> new NotFoundException(User.class, authentication.id))
    );
  }

  @GetMapping(path = "user/connections")
  public ApiResult<List<ConnectedUserDto>> connections(@AuthenticationPrincipal JwtAuthentication authentication) {
    return OK(
      userService.findAllConnectedUser(authentication.id).stream()
        .map(ConnectedUserDto::new)
        .collect(toList())
    );
  }

}