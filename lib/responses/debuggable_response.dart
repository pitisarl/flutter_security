enum DebuggableResponse {
  debugged,
  notDebugged,
  genericError,
}

extension DebuggableResponseExtension on DebuggableResponse {
  static DebuggableResponse fromString(String? response) {
    switch (response) {
      case 'debugged':
        return DebuggableResponse.debugged;
      case 'notDebugged':
        return DebuggableResponse.notDebugged;
      default:
        return DebuggableResponse.genericError;
    }
  }
}
