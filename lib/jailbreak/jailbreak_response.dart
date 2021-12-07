enum JailBreakResponse {
  jailBroken,
  notJailBroken,
  genericError,
}

extension JailBreakResponseExtension on JailBreakResponse {
  static JailBreakResponse fromString(String? response) {
    switch (response) {
      case 'jailBroken':
        return JailBreakResponse.jailBroken;
      case 'notJailBroken':
        return JailBreakResponse.notJailBroken;
      default:
        return JailBreakResponse.genericError;
    }
  }
}
