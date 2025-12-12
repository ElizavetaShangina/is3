function showModalObjectInfo(data) {
    if (data.status === 'success') {
        document.getElementById('modal').style.display = 'block';
    }
}
function hideModalObjectInfo() {
    document.getElementById('modal').style.display = 'none';
}

function showModalChangeInfo(data) {
    if (data.status === 'success') {
        document.getElementById('modalChangeInfo').style.display = 'block';
    }
}

function hideModalChangeInfo() {
    document.getElementById('modalChangeInfo').style.display = 'none';
}

function closeModalAfterAjax(data) {
    if (data.status === 'success') {
        // только после обновления DOM
        document.getElementById('modalChangeInfo').style.display = 'none';
    }
}

function showAddModal() {
    document.getElementById('addModal').style.display = 'block';
}

function closeAddModal() {
    document.getElementById('addModal').style.display = 'none';
}

function closeAddModalAfterAjax(data) {
    if (data.status === 'success') {
        document.getElementById('addModal').style.display = 'none';
    }
}

function showFullNameModal() {
    document.getElementById('fullNameModal').style.display = 'block';
}

function closeFullNameModal() {
    document.getElementById('fullNameModal').style.display = 'none';
}

function closeFullNameModalAfterAjax(data) {
    if (data.status === 'success') {
        document.getElementById('fullNameModal').style.display = 'none';
    }
}

