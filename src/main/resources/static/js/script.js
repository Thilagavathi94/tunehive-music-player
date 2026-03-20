function showSection(event, id){

let sections = [
"latestSection",
"trendingSection",
"topChartsSection",
"topPlaylistsSection",
"podcastsSection",
"topArtistsSection"
];

// hide all
sections.forEach(sec=>{
    let el = document.getElementById(sec);
    if(el) el.style.display="none";
});

// show selected
let active = document.getElementById(id);
if(active) active.style.display="block";

// highlight active menu
document.querySelectorAll(".sidebar-menu li").forEach(li=>{
    li.classList.remove("active");
});

event.target.classList.add("active");

applyFilters();
}

// LANGUAGE FILTER
function applyFilters(){
selectedLangs=[];

document.querySelectorAll('.language-list input:checked')
.forEach(cb=>selectedLangs.push(cb.value));

let images=document.querySelectorAll('.row img');

images.forEach(img=>{
let lang=img.getAttribute("data-lang");

if(selectedLangs.length===0 || selectedLangs.includes(lang)){
img.style.display="block";
}else{
img.style.display="none";
}
});

closeLanguagePopup();
}

// POPUP
function openLanguagePopup(){
document.getElementById("languagePopup").style.display="flex";
}

function closeLanguagePopup(){
document.getElementById("languagePopup").style.display="none";
}

// LOGIN DROPDOWN
function toggleLoginMenu(){
let m=document.getElementById("loginDropdown");
m.style.display=m.style.display==="block"?"none":"block";
}

// DEFAULT LOAD
window.onload=function(){
showAllSections();
};
// 🔁 ROTATE TRENDING ROW (SHIFT LEFT)
function rotateTrending() {
    const row = document.querySelector("#latestSection .row");

    if (!row) return;

    const firstImage = row.children[0]; // first image
    row.appendChild(firstImage); // move to end
}

// run every 2 seconds
setInterval(rotateTrending, 2000);
let interval;

function startRotation(){
    interval = setInterval(rotateTrending, 2000);
}

function stopRotation(){
    clearInterval(interval);
}

const row = document.querySelector("#latestSection .row");

row.addEventListener("mouseenter", stopRotation);
row.addEventListener("mouseleave", startRotation);

startRotation();
// ▶ PLAY BUTTON → GO TO SIGNUP
function goToPlayer(){
    window.location.href="/check-user";
}

// ❤️ WISHLIST BUTTON
function addWishlist(btn){

    if(btn.classList.contains("active")){
        btn.classList.remove("active");
        btn.innerText = "❤️";
    }else{
        btn.classList.add("active");
        btn.innerText = "💚";
    }
}
function showAllSections(){

let sections = [
"latestSection",
"trendingSection",
"topChartsSection",
"topPlaylistsSection",
"podcastsSection",
"topArtistsSection"
];

sections.forEach(sec=>{
    let el = document.getElementById(sec);
    if(el) el.style.display="block";
});

// remove active highlight
document.querySelectorAll(".sidebar-menu li").forEach(li=>{
    li.classList.remove("active");
});

applyFilters();
}