//// Конфигурация
//const API_BASE = '/api/organization';
//const PAGE_SIZE = 10;
//
//// Состояние
//let currentPage = 1;
//let filterName = '';
//let sortField = 'name';
//let sortOrder = 'asc';
//
//// DOM элементы
//const tableBody = document.getElementById('org-table-body');
//const pageInfo = document.getElementById('page-info');
//const prevBtn = document.getElementById('prev-page');
//const nextBtn = document.getElementById('next-page');
//const modal = document.getElementById('modal');
//const modalBody = document.getElementById('modal-body');
//const closeBtn = document.querySelector('.close');
//
//// Инициализация
//document.addEventListener('DOMContentLoaded', () => {
//  setupEventListeners();
//  loadOrganizations();
//  // Автообновление каждые 10 секунд
//  setInterval(loadOrganizations, 10000);
//});
//
//function setupEventListeners() {
//  document.getElementById('btn-add').addEventListener('click', () => openAddModal());
//  document.getElementById('btn-special').addEventListener('click', () => openSpecialModal());
//  document.getElementById('btn-refresh').addEventListener('click', loadOrganizations);
//  document.getElementById('filter-name').addEventListener('input', (e) => {
//    filterName = e.target.value.trim();
//    currentPage = 1;
//    loadOrganizations();
//  });
//  document.getElementById('sort-field').addEventListener('change', (e) => {
//    sortField = e.target.value;
//    loadOrganizations();
//  });
//  document.getElementById('sort-order').addEventListener('change', (e) => {
//    sortOrder = e.target.value;
//    loadOrganizations();
//  });
//  prevBtn.addEventListener('click', () => { if (currentPage > 1) { currentPage--; loadOrganizations(); } });
//  nextBtn.addEventListener('click', () => { currentPage++; loadOrganizations(); });
//  closeBtn.addEventListener('click', closeModal);
//  modal.addEventListener('click', (e) => { if (e.target === modal) closeModal(); });
//}
//
//// Загрузка организаций с фильтрацией и пагинацией (упрощённо)
//async function loadOrganizations() {
//  try {
//    const response = await fetch('/api/organization/all'); // ← НУЖНО РЕАЛИЗОВАТЬ ЭТОТ ENDPOINT!
//    if (!response.ok) throw new Error('Ошибка загрузки');
//    const allOrgs = await response.json();
//
//    // Фильтрация (полное совпадение по имени)
//    let filtered = allOrgs;
//    if (filterName) {
//      filtered = allOrgs.filter(org => org.name === filterName);
//    }
//
//    // Сортировка
//    filtered.sort((a, b) => {
//      const valA = a[sortField] || '';
//      const valB = b[sortField] || '';
//      if (typeof valA === 'string') {
//        return sortOrder === 'asc' ? valA.localeCompare(valB) : valB.localeCompare(valA);
//      }
//      return sortOrder === 'asc' ? valA - valB : valB - valA;
//    });
//
//    // Пагинация
//    const start = (currentPage - 1) * PAGE_SIZE;
//    const paginated = filtered.slice(start, start + PAGE_SIZE);
//
//    renderTable(paginated);
//    pageInfo.textContent = `Страница ${currentPage} из ${Math.max(1, Math.ceil(filtered.length / PAGE_SIZE))}`;
//    prevBtn.disabled = currentPage === 1;
//    nextBtn.disabled = paginated.length < PAGE_SIZE;
//  } catch (err) {
//    alert('Ошибка: ' + err.message);
//  }
//}
//
//function renderTable(orgs) {
//  tableBody.innerHTML = '';
//  orgs.forEach(org => {
//    const row = document.createElement('tr');
//    row.innerHTML = `
//      <td>${org.id}</td>
//      <td>${escapeHtml(org.name)}</td>
//      <td>${escapeHtml(org.fullName)}</td>
//      <td>${org.type}</td>
//      <td>${org.employeesCount}</td>
//      <td>${org.annualTurnover || '—'}</td>
//      <td>${org.rating || '—'}</td>
//      <td>${org.postalAddress?.street || ''}, ${org.postalAddress?.zipCode || ''}</td>
//      <td>
//        <button onclick="viewOrg(${org.id})">Просмотр</button>
//        <button onclick="editOrg(${org.id})">Изменить</button>
//        <button onclick="deleteOrg(${org.id})" style="color:red;">Удалить</button>
//      </td>
//    `;
//    tableBody.appendChild(row);
//  });
//}
//
//// Вспомогательные функции
//function escapeHtml(text) {
//  if (!text) return '';
//  return text.toString().replace(/[&<>"']/g, m => ({
//    '&': '&amp;', '<': '<', '>': '>', '"': '&quot;', "'": '&#039;'
//  }[m]));
//}
//
//function openModal(content) {
//  modalBody.innerHTML = content;
//  modal.classList.remove('hidden');
//}
//
//function closeModal() {
//  modal.classList.add('hidden');
//}
//
//// CRUD и специальные операции
//async function viewOrg(id) {
//  const response = await fetch(`/api/organization/${id}`);
//  const org = await response.json();
//  openModal(`
//    <h3>Организация #${org.id}</h3>
//    <p><strong>Имя:</strong> ${escapeHtml(org.name)}</p>
//    <p><strong>Полное имя:</strong> ${escapeHtml(org.fullName)}</p>
//    <p><strong>Тип:</strong> ${org.type}</p>
//    <p><strong>Сотрудников:</strong> ${org.employeesCount}</p>
//    <p><strong>Адрес:</strong> ${org.postalAddress?.street}, ${org.postalAddress?.zipCode}</p>
//    <!-- Добавьте остальные поля -->
//    <button onclick="closeModal()">Закрыть</button>
//  `);
//}
//
//// Добавьте остальные функции: editOrg, deleteOrg, openAddModal и т.д.
//
//function openAddModal() {
//  openModal(`
//    <h3>Добавить организацию</h3>
//    <form id="add-form">
//      <label>Имя: <input name="name" required></label>
//      <label>Полное имя: <input name="fullName" maxlength="1334" required></label>
//      <label>Тип:
//        <select name="type" required>
//          <option value="COMMERCIAL">COMMERCIAL</option>
//          <option value="GOVERNMENT">GOVERNMENT</option>
//          <option value="TRUST">TRUST</option>
//        </select>
//      </label>
//      <label>Сотрудников: <input type="number" name="employeesCount" min="1" required></label>
//      <label>Годовой оборот: <input type="number" step="0.01" name="annualTurnover"></label>
//      <label>Рейтинг: <input type="number" step="0.1" name="rating"></label>
//
//      <h4>Координаты</h4>
//      <label>X: <input type="number" step="0.1" name="coordinates.x" required></label>
//      <label>Y: <input type="number" name="coordinates.y" min="-460" required></label>
//
//      <h4>Официальный адрес</h4>
//      <label>Улица: <input name="officialAddress.street" required></label>
//      <label>Индекс: <input name="officialAddress.zipCode"></label>
//
//      <h4>Почтовый адрес</h4>
//      <label>Улица: <input name="postalAddress.street" required></label>
//      <label>Индекс: <input name="postalAddress.zipCode"></label>
//
//      <button type="submit">Создать</button>
//      <button type="button" onclick="closeModal()">Отмена</button>
//    </form>
//  `);
//  document.getElementById('add-form').addEventListener('submit', handleAddSubmit);
//}
//
//async function handleAddSubmit(e) {
//  e.preventDefault();
//  const form = e.target;
//  const formData = new FormData(form);
//  const obj = {};
//  for (let [key, value] of formData.entries()) {
//    if (key.includes('.')) {
//      const parts = key.split('.');
//      if (!obj[parts[0]]) obj[parts[0]] = {};
//      obj[parts[0]][parts[1]] = value;
//    } else {
//      obj[key] = value;
//    }
//  }
//  // Приведение типов
//  obj.employeesCount = parseInt(obj.employeesCount);
//  if (obj.annualTurnover) obj.annualTurnover = parseFloat(obj.annualTurnover);
//  if (obj.rating) obj.rating = parseFloat(obj.rating);
//  obj.coordinates.x = parseFloat(obj.coordinates.x);
//  obj.coordinates.y = parseInt(obj.coordinates.y);
//
//  try {
//    const res = await fetch(API_BASE + '/add-organization', {
//      method: 'POST',
//      headers: { 'Content-Type': 'application/json' },
//      body: JSON.stringify(obj)
//    });
//    if (res.ok) {
//      closeModal();
//      loadOrganizations();
//    } else {
//      const err = await res.text();
//      alert('Ошибка: ' + err);
//    }
//  } catch (err) {
//    alert('Ошибка сети: ' + err.message);
//  }
//}
//
//function openSpecialModal() {
//  openModal(`
//    <h3>Специальные операции</h3>
//    <button onclick="getMaxFullName()">Организация с максимальным fullName</button><br><br>
//    <button onclick="countByPostalAddressPrompt()">Количество по почтовому адресу</button><br><br>
//    <button onclick="countByTypeLessThanPrompt()">Количество по типу (меньше)</button><br><br>
//    <button onclick="mergeOrganizationsPrompt()">Объединить организации</button><br><br>
//    <button onclick="absorbOrganizationsPrompt()">Поглотить организацию</button><br><br>
//    <button onclick="closeModal()">Закрыть</button>
//  `);
//}
//
//// Реализуйте остальные функции по аналогии
//
//async function getMaxFullName() {
//  const res = await fetch(API_BASE + '/get-organization-with-max-full-name');
//  const org = await res.json();
//  if (org) {
//    alert(`Найдена: ${org.fullName} (ID: ${org.id})`);
//  } else {
//    alert('Организаций нет');
//  }
//}
