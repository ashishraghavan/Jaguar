function getUrlParameter(queryKey) {
    var pageUrl = window.location.search.substring(1);
    var token = pageUrl.split('&');
    for (var i = 0; i < token.length; i++) {
        var tokenKeyArray = token[i].split('=');
        if (tokenKeyArray[0] === queryKey) {
            return tokenKeyArray[1];
        }
    }
}