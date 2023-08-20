describe('template spec', () => {
  it('passes', () => {
    cy.visit('http://localhost:5173');
    cy.contains('车站管理').click();
    cy.get('[placeholder="新车站名"]').type('北京站')
    cy.contains('添加').click();
  })
})