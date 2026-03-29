let playCount = 0;

function playSong(){

    let audio = document.getElementById("audioPlayer");

    if(playCount === 0){
        audio.play()
        .then(() => {
            console.log("Playing...");
        })
        .catch(err => {
            console.error("Play error:", err);
        });

        playCount++;
    }else{
        document.getElementById("limitPopup").style.display = "flex";
    }
}
audio.muted = false;
audio.volume = 1;
function logout(){

    sessionStorage.clear();   // 🔥 important

    window.location.href = "/";  // go to home
}