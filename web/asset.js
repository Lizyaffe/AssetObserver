(function (asset) {

    asset.submitForm = function (form) {
        var url = '/api';
        var params = '';
        var jsonObj = new Object();
        for (var i = 0; i < form.elements.length; i++) {
            if (!form.elements[i].name) {
                continue;
            }
            jsonObj[form.elements[i].name] = form.elements[i].value;
            if (i > 0) {
                params += '&';
            }
            params += encodeURIComponent(form.elements[i].name);
            params += '=';
            params += encodeURIComponent(form.elements[i].value);
        }
        var request = new XMLHttpRequest();
        request.open("POST", url, false);
        request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
        request.send(params);
        var result = JSON.stringify(JSON.parse(request.responseText), null, 4);
        document.getElementById('request').textContent = document.location + url.substring(1) + '?' + params + "&p=" + JSON.stringify(jsonObj);
        form.getElementsByClassName("result")[0].textContent = result;
        return false;
    };

    asset.submitFormBackup = function (form) {
        var url = '/api';
        var params = '';
        for (var i = 0; i < form.elements.length; i++) {
            if (!form.elements[i].name) {
                continue;
            }
            if (i > 0) {
                params += '&';
            }
            params += encodeURIComponent(form.elements[i].name);
            params += '=';
            params += encodeURIComponent(form.elements[i].value);
        }
        var request = new XMLHttpRequest();
        request.open("POST", url, false);
        request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
        request.send(params);
        var result = JSON.stringify(JSON.parse(request.responseText), null, 4);
        document.getElementById('request').textContent = document.location + url.substring(1) + '?' + params;
        form.getElementsByClassName("result")[0].textContent = result;
        return false;
    };

    asset.log = function (msg) {
        if (!window.console) {
            return;
        }
        console.log(msg);
    };


}(window.asset = window.asset || {}));