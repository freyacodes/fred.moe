var pendingFiles = [];
var curUploadDiv;
var uploadSize;

// Fired when the user drops files for us to upload
function handleFileSelect(evt) {
    evt.stopPropagation();
    evt.preventDefault();

    var files = evt.dataTransfer.files; // FileList object.

    var fl = files.length;

    // Push into array for async processing
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
    evt.dataTransfer.dropEffect = 'copy';
}

function uploadNext() {
    // Break the loop if we are out of files to upload
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
            // Notify the user that the upload is finished, and that we have a file URL.
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

    // This attaches the file and executes the POST request to /upload
    xhr.send(formData)
}

// Utility function to show the upload progress
function updateProgress(loaded, total) {
    curUploadDiv.innerHTML = (loaded + "/" + total);
}

function onload() {
    // Setup the dnd listeners.
    var dropZone = document.getElementById('dropzone');
    dropZone.addEventListener('dragover', handleDragOver, false);
    dropZone.addEventListener('drop', handleFileSelect, false);

    // Figure out the name of the site, or otherwise just the IP
    var loc = window.location.origin.replace(/https?:\/\//, "");
    loc = loc.replace(/:\d+/, "");

    // Show the current version
    document.getElementById('footer').innerHTML = loc + ' ~ v2.0 ~ <a href="https://github.com/Frederikam/fred.moe">about</a>';
}

