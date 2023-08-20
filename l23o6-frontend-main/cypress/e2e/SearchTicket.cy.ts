describe('template spec', () => {
  it('passes', () => {
    cy.visit('http://localhost:5173')
    cy.contains('车站管理').click();
    cy.get('[]')
  })
})