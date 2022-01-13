enum DebuggableResponse {
  debbugable,
  notDebbugable,
  genericError,
}

extension DebuggableResponseExtension on DebuggableResponse {
  static DebuggableResponse fromString(String? response) {
    switch (response) {
      case 'debbugable':
        return DebuggableResponse.debbugable;
      case 'notDebbugable':
        return DebuggableResponse.notDebbugable;
      default:
        return DebuggableResponse.genericError;
    }
  }
}
