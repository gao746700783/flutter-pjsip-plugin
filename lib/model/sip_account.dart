class SipAccount {
  final String port;
  final String displayName;
  final String? sipUri;
  final String password;
  final String authUser;

  SipAccount({
    required this.port,
    required this.displayName,
    required this.password,
    required this.authUser,
    this.sipUri,
  });

  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;
    return other is SipAccount &&
        other.port == port &&
        other.displayName == displayName &&
        other.sipUri == sipUri &&
        other.password == password &&
        other.authUser == authUser;
  }

  @override
  int get hashCode {
    return Object.hashAll([
      port,
      displayName,
      sipUri,
      password,
      authUser,
    ]);
  }
}
