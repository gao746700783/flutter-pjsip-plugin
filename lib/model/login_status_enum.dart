/// app状态
// ignore_for_file: unreachable_switch_default

enum LoginStatusEnum {
  // 注册的初始状态
  none(0),
  // 登记正在进行中
  progress(1),
  // 注册成功
  ok(2),
  // 注销成功
  cleared(3),
  // 注册失败
  failed(4),
  // 正在刷新注册
  refreshing(5);

  final int code;

  const LoginStatusEnum(this.code);

  int getCode() {
    return code;
  }

  static LoginStatusEnum fromInt(int value) {
    switch (value) {
      case 0:
        return LoginStatusEnum.none;
      case 1:
        return LoginStatusEnum.progress;
      case 2:
      case 200:
        return LoginStatusEnum.ok;
      case 3:
        return LoginStatusEnum.cleared;
      case 4:
        return LoginStatusEnum.failed;
      case 5:
        return LoginStatusEnum.refreshing;
      default:
        return LoginStatusEnum.none;
    }
  }
}

extension LoginStatusEnumExtension on LoginStatusEnum {
  bool isLoggedIn() {
    return this == LoginStatusEnum.ok;
  }

  String getLoginStatusString() {
    switch (this) {
      case LoginStatusEnum.none:
        return "None";
      case LoginStatusEnum.progress:
        return "Progress";
      case LoginStatusEnum.ok:
        return "Logged In";
      case LoginStatusEnum.cleared:
        return "Cleared";
      case LoginStatusEnum.failed:
        return "Failed";
      case LoginStatusEnum.refreshing:
        return "Refreshing";
      default:
        return "Unknown";
    }
  }
}
