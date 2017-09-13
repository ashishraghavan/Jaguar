<html>
<body>

<script src="Utils.js"></script>
<script type="text/javascript">
    var authorizationCode = getUrlParameter('authorization_code');
    var clientId = getUrlParameter('client_id');
    if(authorizationCode === '' || authorizationCode === null || authorizationCode === undefined) {
        console.log('The authorization code obtained was null/empty/undefined. Can\'t continue');
        alert('The authorization code obtained was null/empty/undefined. Can\'t continue');
    }

    if(clientId === '' || clientId === null || clientId === undefined) {
        console.log('The client id obtained from the re-direction URL was null/empty/undefined.');
        alert('The client id obtained from the re-direction URL was null/empty/undefined.');
    }

    var tokenRequest = new XMLHttpRequest();
    var tokenURL = location.protocol + '//' + location.hostname + (location.port ? ':' + location.port : '') +
        "/" + "api/oauth/token";
    var formData = new FormData();
    formData.set('authorization_code',authorizationCode);
    formData.set('client_id',clientId);
    tokenRequest.open('POST',tokenURL,true);
    tokenRequest.onreadystatechange = function () {
        var jsonResponse;
        var defaultErrorMessage = 'An unknown error occurred while processing this request';
        if(tokenRequest.readyState === tokenRequest.DONE) {
            if(tokenRequest.status === 200) {
                //If we got the token request, try parsing the JSON response.
                try {
                    jsonResponse = JSON.parse(tokenRequest.responseText);
                    var accessToken = jsonResponse['access_token'];
                    //Get the access_token parameter and store it somewhere.
                    if(accessToken === null || accessToken === undefined) {
                        alert('No access token was obtained from the response. Please re-try');
                        return;
                    }
                    //We got the token.
                    localStorage.setItem('access_token',accessToken);
                } catch(err) {
                    console.log(defaultErrorMessage + err.toString());
                    alert('There was an error processing the JSON response.')
                }
                return;
            }
            //If this was not a 200 response.
            try {
                jsonResponse = JSON.parse(tokenRequest.responseText);
                //See if we can get the errorMessage value to display as an alert.
                if(jsonResponse['errorMessage'] === null || jsonResponse['errorMessage'] === undefined) {
                    alert(defaultErrorMessage);
                    return;
                }
                alert(jsonResponse['errorMessage']);
            } catch(err) {
                console.log(defaultErrorMessage + err.toString());
                alert(defaultErrorMessage);
            }
        }
    };
    tokenRequest.send(formData);
</script>
<h2>Hello World!</h2>
</body>
</html>
