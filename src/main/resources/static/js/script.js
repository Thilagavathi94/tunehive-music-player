/*
 * TUNE HIVE — script.js (Complete Rewrite)
 * Single auth model: sessionStorage is the truth for frontend.
 * Spring HttpSession is the truth for backend routes.
 * Both are set together at login/signup so they stay in sync.
 */

/* ════════════════════════════════════════════════
   AUTH
   ════════════════════════════════════════════════ */
function isLoggedIn() {
  return sessionStorage.getItem("loggedIn") === "true";
}

function isPremium() {
  return sessionStorage.getItem("premium") === "true";
}

/* ════════════════════════════════════════════════
   NAVBAR
   ════════════════════════════════════════════════ */
function updateNavbar() {
  var g = document.getElementById("guestMenu");
  var u = document.getElementById("userMenu");
  if (isLoggedIn()) {
    if (g) g.style.display = "none";
    if (u) u.style.display = "flex";
  } else {
    if (g) g.style.display = "flex";
    if (u) u.style.display = "none";
  }
}

/* ════════════════════════════════════════════════
   DROPDOWN
   ════════════════════════════════════════════════ */
function toggleDropdown() {
  var c = document.getElementById("dropdownContent");
  if (c) c.classList.toggle("open");
}

document.addEventListener("click", function(e) {
  var d = document.getElementById("menuDropdown");
  if (d && !d.contains(e.target)) {
    var c = document.getElementById("dropdownContent");
    if (c) c.classList.remove("open");
  }
});

/* ════════════════════════════════════════════════
   SIDEBAR SECTIONS
   ════════════════════════════════════════════════ */
var SECTIONS = [
  "latestSection","trendingSection","topChartsSection",
  "topPlaylistsSection","podcastsSection","topArtistsSection"
];

function showSection(event, id) {
  SECTIONS.forEach(function(sec) {
    var el = document.getElementById(sec);
    if (el) el.style.display = "none";
  });
  var active = document.getElementById(id);
  if (active) active.style.display = "block";
  document.querySelectorAll(".sidebar-menu li").forEach(function(li) {
    li.classList.remove("active");
  });
  if (event && event.target) event.target.classList.add("active");
}

function showAllSections() {
  SECTIONS.forEach(function(sec) {
    var el = document.getElementById(sec);
    if (el) el.style.display = "block";
  });
  document.querySelectorAll(".sidebar-menu li").forEach(function(li) {
    li.classList.remove("active");
  });
}

/* ════════════════════════════════════════════════
   LANGUAGE FILTER
   ════════════════════════════════════════════════ */
function applyFilters() {
  var selected = [];
  document.querySelectorAll(".language-list input:checked").forEach(function(cb) {
    selected.push(cb.value);
  });

  document.querySelectorAll(".row > img").forEach(function(img) {
    var lang = img.getAttribute("data-lang");
    img.style.display = (!selected.length || selected.indexOf(lang) !== -1) ? "block" : "none";
  });

  document.querySelectorAll(".row .card").forEach(function(card) {
    var img  = card.querySelector("img");
    var lang = img ? img.getAttribute("data-lang") : null;
    card.style.display = (!selected.length || selected.indexOf(lang) !== -1) ? "block" : "none";
  });

  closeLanguagePopup();
}

function openLanguagePopup() {
  var p = document.getElementById("languagePopup");
  if (p) p.style.display = "flex";
}

function closeLanguagePopup() {
  var p = document.getElementById("languagePopup");
  if (p) p.style.display = "none";
}

/* ════════════════════════════════════════════════
   SEARCH
   ════════════════════════════════════════════════ */
function searchSongs() {
  var input = document.getElementById("searchInput");
  if (!input) return;
  var q = input.value.toLowerCase();
  document.querySelectorAll(".card").forEach(function(card) {
    var title = (card.getAttribute("data-title") || "").toLowerCase();
    card.style.display = (!q || title.indexOf(q) !== -1) ? "block" : "none";
  });
}

/* ════════════════════════════════════════════════
   LOGOUT
   ════════════════════════════════════════════════ */
function logout() {
  sessionStorage.clear();
  window.location.href = "/custom-logout";
}

/* ════════════════════════════════════════════════
   WISHLIST
   ════════════════════════════════════════════════ */
function addWishlist(btn) {
  var card = btn.closest(".card") || btn.closest(".movie-card");
  if (!card) return;
  var imgEl = card.querySelector("img");
  if (!imgEl) return;
  var src = imgEl.src;

  var wishlist = JSON.parse(localStorage.getItem("wishlist") || "[]");
  if (wishlist.indexOf(src) !== -1) {
    wishlist = wishlist.filter(function(i) { return i !== src; });
    btn.innerText = "❤️";
    btn.classList.remove("active");
  } else {
    wishlist.push(src);
    btn.innerText = "💚";
    btn.classList.add("active");
  }
  localStorage.setItem("wishlist", JSON.stringify(wishlist));
}

/* ════════════════════════════════════════════════
   AUDIO PLAYER
   All songs play freely once logged in.
   Non-premium users: limit 3 songs then prompt upgrade.
   ════════════════════════════════════════════════ */
var _audioEl   = null;
var _currentBtn = null;

function getAudio() {
  if (!_audioEl) _audioEl = document.getElementById("audioPlayer");
  return _audioEl;
}

/*
 * playSong(src, btn)
 * Called from index.html and player.html cards.
 */
window.playSong = function(src, btn) {
  var audio = getAudio();
  if (!audio) return;

  if (!isLoggedIn()) {
    localStorage.setItem("pendingSong", src);
    window.location.href = "/login";
    return;
  }

  // Premium / free limit check
  if (!isPremium()) {
    var count = parseInt(localStorage.getItem("playCount") || "0");
    if (count >= 3) {
      if (confirm("You've played 3 free songs. Upgrade to PRO for unlimited listening?")) {
        window.location.href = "/plans";
      }
      return;
    }
  }

  // Toggle same song
  var curFile = audio.src ? audio.src.split("/").pop() : "";
  var newFile = src.split("/").pop();

  if (curFile === newFile && audio.src) {
    if (audio.paused) {
      audio.play().catch(function(){});
      if (btn) btn.innerText = "⏸";
    } else {
      audio.pause();
      if (btn) btn.innerText = "▶";
    }
    return;
  }

  // New song
  audio.pause();
  if (_currentBtn) _currentBtn.innerText = "▶";

  audio.src = src;
  audio.load();
  audio.play().catch(function(){});

  if (btn) btn.innerText = "⏸";
  _currentBtn = btn;

  if (!isPremium()) {
    var c = parseInt(localStorage.getItem("playCount") || "0") + 1;
    localStorage.setItem("playCount", String(c));
  }

  saveHistory(src, btn);
};

/*
 * playAnySong(element)
 * Called from dashboard.html cards (element has data-song attr).
 */
let playCount = parseInt(localStorage.getItem("playCount")) || 0;

function playSong(src, btn) {

  if (!isLoggedIn()) {
    window.location.href = "/signup";
    return;
  }

  if (!isPremium() && playCount >= 1) {
    alert("Limit reached! Upgrade plan.");
    window.location.href = "/plans";
    return;
  }

  let audio = document.getElementById("audioPlayer");
  audio.src = src;
  audio.play();

  if (!isPremium()) {
    playCount++;
    localStorage.setItem("playCount", playCount);
  }
}
/*
 * loadSong(src, img, title)
 * Called from player.html full player.
 */
window.loadSong = function(src, img, title) {
  var audio = getAudio();
  if (!audio) return;

  if (!isLoggedIn()) {
    window.location.href = "/login";
    return;
  }

  audio.src = src;
  audio.load();
  audio.play().catch(function(){});

  var pi = document.getElementById("playerImage");
  var pt = document.getElementById("playerTitle");
  var pb = document.getElementById("playBtn");
  if (pi) pi.src = img;
  if (pt) pt.innerText = title;
  if (pb) pb.innerText = "⏸";
};

/* Toggle play/pause (player bar) */
function togglePlay() {
  var audio = getAudio();
  if (!audio) return;
  if (audio.paused) {
    audio.play().catch(function(){});
    var pb = document.getElementById("playBtn");
    if (pb) pb.innerText = "⏸";
  } else {
    audio.pause();
    var pb = document.getElementById("playBtn");
    if (pb) pb.innerText = "▶";
  }
}

/* ════════════════════════════════════════════════
   HISTORY
   ════════════════════════════════════════════════ */
function saveHistory(src, btn) {
  try {
    var card   = btn ? (btn.closest(".card") || btn.closest(".movie-card")) : null;
    var imgEl  = card ? card.querySelector("img") : null;
    var imgSrc = imgEl ? imgEl.src : "";
    var title  = card ? (card.getAttribute("data-title") || src.split("/").pop()) : src.split("/").pop();

    var history = JSON.parse(localStorage.getItem("history") || "[]");
    history = history.filter(function(h) { return h.src !== src; });
    history.unshift({ src: src, img: imgSrc, title: title });
    if (history.length > 50) history = history.slice(0, 50);
    localStorage.setItem("history", JSON.stringify(history));
  } catch(e) {}
}

/* ════════════════════════════════════════════════
   CAROUSEL
   ════════════════════════════════════════════════ */
var _carousel = null;

function rotateTrending() {
  var row = document.querySelector("#latestSection .row");
  if (!row || row.children.length === 0) return;
  row.appendChild(row.children[0]);
}

function startRotation() {
  clearInterval(_carousel);
  _carousel = setInterval(rotateTrending, 2500);
}

function stopRotation() {
  clearInterval(_carousel);
}

/* ════════════════════════════════════════════════
   DOM READY
   ════════════════════════════════════════════════ */
document.addEventListener("DOMContentLoaded", function() {

  updateNavbar();

  // Play pending song if we just returned from login
  var pending = localStorage.getItem("pendingSong");
  if (pending && isLoggedIn()) {
    var audio = getAudio();
    if (audio) {
      audio.src = pending;
      audio.load();
      audio.play().catch(function(){});
    }
    localStorage.removeItem("pendingSong");
  }

  // Carousel
  var row = document.querySelector("#latestSection .row");
  if (row) {
    row.addEventListener("mouseenter", stopRotation);
    row.addEventListener("mouseleave", startRotation);
    startRotation();
  }
});