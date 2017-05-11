var pendingFiles = [];
var curUploadDiv;
var uploadSize;

function handleFileSelect(evt) {
    evt.stopPropagation();
    evt.preventDefault();

    var files = evt.dataTransfer.files; // FileList object.

    var fl = files.length;

    for (var i = 0; i < fl; i++) {
        pendingFiles.push(files[i]);
    }

    // Make sure we are not already looping
    if(pendingFiles.length === files.length)
        uploadNext();
}

function handleDragOver(evt) {
    evt.stopPropagation();
    evt.preventDefault();
    evt.dataTransfer.dropEffect = 'copy'; // Explicitly show this is a copy.
}

function uploadNext() {
    if(pendingFiles.length === 0)
        return;

    var file = pendingFiles.pop();
    uploadSize = file.size;

    console.log(file);

    var formData = new FormData();
    formData.append("file", file);

    var xhr = new XMLHttpRequest();
    xhr.open("POST", window.location.origin + "/upload", true);
    xhr.onreadystatechange = function () {
        if(xhr.readyState === XMLHttpRequest.DONE && xhr.status === 200) {
            console.log(xhr.responseText);
            updateProgress(uploadSize, uploadSize);
            var url = JSON.parse(xhr.responseText).files[0].url;
            curUploadDiv.innerHTML = '<a href="' + url + '">' + url + '</a>';
            uploadNext();
        }
    };

    xhr.onerror = function (e) {
        curUploadDiv.innerHTML = "Error";
        uploadNext();
    };

    xhr.onprogress = function (e) {
        console.log(e);
        if(e.loaded !== e.total)
         updateProgress(e.loaded, e.total);
    };

    document.getElementById("file-log").insertAdjacentHTML("beforeend",
        '<div class="file-progress">' +
            '0%' +
        '</div>');


    curUploadDiv = document.getElementById("file-log").lastElementChild;

    updateProgress(0, uploadSize);

    xhr.send(formData)
}

function updateProgress(loaded, total) {
    var width = (100 * (loaded / total)) + "%";
    curUploadDiv.innerHTML = (loaded + "/" + total);
}

function onload() {
    // Setup the dnd listeners.
    var dropZone = document.getElementById('dropzone');
    dropZone.addEventListener('dragover', handleDragOver, false);
    dropZone.addEventListener('drop', handleFileSelect, false);

    var loc = window.location.origin.replace(/https?:\/\//, "");
    loc = loc.replace(/:\d+/, "");

    document.getElementById('footer').innerHTML = loc + ' ~ v2.0 ~ <a href="https://github.com/Frederikam/fred.moe">about</a>';
}

